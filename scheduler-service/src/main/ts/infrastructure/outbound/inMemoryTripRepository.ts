import { Trip } from "../../domain/model/trip";
import { TripRepository } from "../../domain/ports/tripRepository";

const copy = (t: Trip): Trip => ({
  ...t,
  origin: { ...t.origin },
  stops: t.stops.map((s) => ({ ...s, location: { ...s.location } })),
  // The route is immutable once planned: it is shared, not copied point by point.
  progress: t.progress ? { ...t.progress, position: { ...t.progress.position } } : null,
});

/** Process-local trip store, same caveats as InMemoryBookingRepository. */
export class InMemoryTripRepository implements TripRepository {
  private readonly byId = new Map<string, Trip>();

  async save(trip: Trip): Promise<void> {
    this.byId.set(trip.id, copy(trip));
  }

  async update(trip: Trip): Promise<void> {
    if (!this.byId.has(trip.id)) throw new Error(`Unknown trip ${trip.id}`);
    this.byId.set(trip.id, copy(trip));
  }

  async findById(id: string): Promise<Trip | null> {
    const found = this.byId.get(id);
    return found ? copy(found) : null;
  }

  async findInProgress(): Promise<Trip[]> {
    return [...this.byId.values()].filter((t) => t.progress && !t.progress.finished).map(copy);
  }

  async findBySlot(restaurantId: string, slotStartEpochMs: number): Promise<Trip[]> {
    return [...this.byId.values()]
      .filter((t) => t.restaurantId === restaurantId && t.slotStartEpochMs === slotStartEpochMs)
      .map(copy);
  }
}
