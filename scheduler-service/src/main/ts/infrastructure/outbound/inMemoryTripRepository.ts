import { Trip } from "../../domain/model/trip";
import { TripRepository } from "../../domain/ports/tripRepository";

const copy = (t: Trip): Trip => ({ ...t, stops: t.stops.map((s) => ({ ...s, location: { ...s.location } })) });

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

  async findBySlot(restaurantId: string, slotStartEpochMs: number): Promise<Trip[]> {
    return [...this.byId.values()]
      .filter((t) => t.restaurantId === restaurantId && t.slotStartEpochMs === slotStartEpochMs)
      .map(copy);
  }
}
