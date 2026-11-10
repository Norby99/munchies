import { Trip } from "../model/trip";

export interface TripRepository {
  save(trip: Trip): Promise<void>;
  update(trip: Trip): Promise<void>;
  findBySlot(restaurantId: string, slotStartEpochMs: number): Promise<Trip[]>;
}
