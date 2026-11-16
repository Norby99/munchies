import { Trip } from "../model/trip";

export interface TripRepository {
  save(trip: Trip): Promise<void>;
  update(trip: Trip): Promise<void>;
  findById(id: string): Promise<Trip | null>;
  /** Trips whose driver has left the restaurant and is not back yet. */
  findInProgress(): Promise<Trip[]>;
  findBySlot(restaurantId: string, slotStartEpochMs: number): Promise<Trip[]>;
}
