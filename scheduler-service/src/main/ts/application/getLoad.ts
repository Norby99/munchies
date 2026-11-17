import { generateSlots } from "../domain/model/availability";
import { BookingRepository } from "../domain/ports/bookingRepository";
import { TripRepository } from "../domain/ports/tripRepository";
import { PolicyResolver } from "./policyResolver";

export interface LoadSlot {
  start: string;
  end: string;
  startEpochMs: number;
  bookedUnits: number;
  capacityUnits: number;
  remainingUnits: number;
  tripCount: number;
}

export interface LoadResult {
  restaurantId: string;
  date: string;
  maxCapacityUnits: number;
  maxTripsPerSlot: number;
  totalBookedUnits: number;
  totalCapacityUnits: number;
  slots: LoadSlot[];
}

/** Manager view: kitchen units and delivery trips used vs. available for each slot of a day. */
export class GetLoad {
  constructor(
    private readonly policies: PolicyResolver,
    private readonly bookings: BookingRepository,
    private readonly trips: TripRepository,
  ) {}

  async execute(restaurantId: string, dateIso: string): Promise<LoadResult> {
    const policy = await this.policies.resolve(restaurantId);
    const slots = generateSlots(dateIso, policy);
    const used = await this.bookings.sumActiveWeightBySlot(restaurantId, slots.map((s) => s.startEpochMs));

    const loadSlots: LoadSlot[] = [];
    for (const s of slots) {
      const booked = used.get(s.startEpochMs) ?? 0;
      const tripCount = (await this.trips.findBySlot(restaurantId, s.startEpochMs)).length;
      loadSlots.push({
        start: s.start,
        end: s.end,
        startEpochMs: s.startEpochMs,
        bookedUnits: booked,
        capacityUnits: policy.maxCapacityUnits,
        remainingUnits: Math.max(0, policy.maxCapacityUnits - booked),
        tripCount,
      });
    }

    return {
      restaurantId,
      date: dateIso,
      maxCapacityUnits: policy.maxCapacityUnits,
      maxTripsPerSlot: policy.maxTripsPerSlot,
      totalBookedUnits: loadSlots.reduce((sum, s) => sum + s.bookedUnits, 0),
      totalCapacityUnits: loadSlots.length * policy.maxCapacityUnits,
      slots: loadSlots,
    };
  }
}
