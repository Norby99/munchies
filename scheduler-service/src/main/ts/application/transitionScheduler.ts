import { BookingRepository } from "../domain/ports/bookingRepository";
import { OrderClient, OrderServiceError } from "../domain/ports/orderClient";

/**
 * The scheduler's second job: driving timed order-state changes. On every tick it
 * finds CONFIRMED bookings whose prep time has arrived and tells order-service to
 * advance them (PENDING -> PREPARING).
 *
 * A booking is only advanced once here; later transitions (READY, ON_THE_WAY,
 * COMPLETED) stay manager-driven via AdvanceScheduledOrder.
 */
export class TransitionScheduler {
  private timer: NodeJS.Timeout | null = null;
  private running = false;

  constructor(
    private readonly bookings: BookingRepository,
    private readonly orders: OrderClient,
    private readonly tickMs: number,
    private readonly now: () => number = () => Date.now(),
  ) {}

  start(): void {
    if (this.timer) return;
    this.timer = setInterval(() => {
      void this.tick();
    }, this.tickMs);
    this.timer.unref?.();
  }

  stop(): void {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
  }

  /** Exposed for tests and for an eager run right after startup. */
  async tick(): Promise<{ advanced: number; failed: number }> {
    if (this.running) return { advanced: 0, failed: 0 };
    this.running = true;
    let advanced = 0;
    let failed = 0;
    try {
      const due = await this.bookings.findDueForPrep(this.now());
      for (const booking of due) {
        if (!booking.orderId) continue;
        try {
          await this.orders.advance(booking.orderId);
          booking.status = "PREPARING";
          booking.updatedAtEpochMs = this.now();
          await this.bookings.update(booking);
          advanced += 1;
        } catch (err) {
          if (err instanceof OrderServiceError && err.status >= 400 && err.status < 500) {
            // Order already moved on (or is gone) — stop chasing it.
            booking.status = "PREPARING";
            booking.updatedAtEpochMs = this.now();
            await this.bookings.update(booking);
            advanced += 1;
          } else {
            failed += 1;
            console.warn(
              `[transitions] could not advance order ${booking.orderId}:`,
              err instanceof Error ? err.message : err,
            );
          }
        }
      }
    } finally {
      this.running = false;
    }
    if (advanced || failed) {
      console.info(`[transitions] tick done: advanced=${advanced} failed=${failed}`);
    }
    return { advanced, failed };
  }
}
