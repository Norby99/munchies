import { advanceTrip } from "../domain/model/tripProgress";
import { TripRepository } from "../domain/ports/tripRepository";

/**
 * Mock of the drivers' devices. No real driver reports a position, so on every tick this moves
 * each departed trip's driver along its planned route at a constant speed and stores the new position,
 * exactly where a real position report would be written. GetOrderTracking only ever reads the
 * stored position: replacing this class with an inbound "driver reports position" adapter would
 * not change the read side.
 */
export class DriverSimulator {
  private timer: NodeJS.Timeout | null = null;
  private running = false;

  constructor(
    private readonly trips: TripRepository,
    private readonly tickMs: number,
    private readonly speedKmh: number,
    private readonly now: () => number = () => Date.now(),
  ) {
    if (!(tickMs > 0)) throw new Error("driver tick must be > 0 ms");
    if (!(speedKmh > 0)) throw new Error("driver speed must be > 0 km/h");
  }

  start(): void {
    if (this.timer) return;
    this.timer = setInterval(() => {
      void this.tick().catch((err) => {
        console.warn("[drivers] tick failed:", err instanceof Error ? err.message : err);
      });
    }, this.tickMs);
    this.timer.unref?.();
  }

  stop(): void {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
  }

  /** Exposed for tests. Returns how many drivers moved and how many of them got back. */
  async tick(): Promise<{ moved: number; finished: number }> {
    if (this.running) return { moved: 0, finished: 0 };
    this.running = true;
    let moved = 0;
    let finished = 0;
    try {
      for (const trip of await this.trips.findInProgress()) {
        if (!trip.progress) continue;
        trip.progress = advanceTrip(trip.progress, this.speedKmh, this.now());
        await this.trips.update(trip);
        moved += 1;
        if (trip.progress.finished) {
          finished += 1;
          console.info(`[drivers] trip ${trip.id} is back at the restaurant`);
        }
      }
    } finally {
      this.running = false;
    }
    return { moved, finished };
  }
}
