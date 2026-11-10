import { AvailableRange, availableRanges, generateSlots } from "../domain/model/availability";
import { CapacityPolicy } from "../domain/model/capacityPolicy";
import { OrderType } from "../domain/model/booking";
import { GeoPoint, isValidGeoPoint } from "../domain/model/geo";
import { planDeliveryPlacement } from "../domain/model/trip";
import { BookingRepository } from "../domain/ports/bookingRepository";
import { TripRepository } from "../domain/ports/tripRepository";
import { DeliveryGate } from "./deliveryGate";
import { PolicyResolver } from "./policyResolver";
import { badRequest } from "./errors";

export interface AvailabilityQuery {
  restaurantId: string;
  dateIso: string;
  orderType?: OrderType;
  /** Required for DELIVERY: a slot is only offered if a trip can take this address. */
  deliveryLocation?: GeoPoint;
}

export interface AvailabilityResult {
  restaurantId: string;
  date: string;
  orderType: OrderType | null;
  slotMinutes: number;
  maxCapacityUnits: number;
  ranges: AvailableRange[];
}

const ORDER_TYPES: OrderType[] = ["DELIVERY", "TAKEAWAY", "DINE_IN"];

/**
 * "Given how many orders I have now and how many I can handle, what hours are open?"
 * For DELIVERY it also asks whether a trip can take this address in each slot, so the
 * customer is never offered a slot that would fail at confirmation.
 */
export class GetAvailability {
  constructor(
    private readonly policies: PolicyResolver,
    private readonly bookings: BookingRepository,
    private readonly trips: TripRepository,
    private readonly delivery: DeliveryGate,
    private readonly now: () => number = () => Date.now(),
  ) {}

  async execute(query: AvailabilityQuery): Promise<AvailabilityResult> {
    if (query.orderType && !ORDER_TYPES.includes(query.orderType)) {
      throw badRequest(`Unknown orderType "${query.orderType}"`);
    }
    const isDelivery = query.orderType === "DELIVERY";
    if (isDelivery && !isValidGeoPoint(query.deliveryLocation)) {
      throw badRequest("deliveryLocation {lat, lng} is required for DELIVERY availability");
    }

    let policy: CapacityPolicy;
    let origin: GeoPoint | null = null;
    if (isDelivery) {
      const setup = await this.delivery.resolve(query.restaurantId);
      if (!setup) {
        return {
          restaurantId: query.restaurantId,
          date: query.dateIso,
          orderType: query.orderType ?? null,
          slotMinutes: 0,
          maxCapacityUnits: 0,
          ranges: [],
        };
      }
      policy = setup.policy;
      origin = setup.origin;
    } else {
      policy = await this.policies.resolve(query.restaurantId);
    }

    const slots = generateSlots(query.dateIso, policy); // throws RangeError -> mapped to 400
    const booked = await this.bookings.sumActiveWeightBySlot(
      query.restaurantId,
      slots.map((s) => s.startEpochMs),
    );
    let ranges = availableRanges(slots, booked, policy, this.now());

    if (isDelivery && origin && query.deliveryLocation) {
      const point = query.deliveryLocation;
      const feasible: AvailableRange[] = [];
      for (const range of ranges) {
        const trips = await this.trips.findBySlot(query.restaurantId, range.startEpochMs);
        const plan = planDeliveryPlacement(origin, trips, point, policy);
        if (plan.kind !== "rejected") feasible.push(range);
      }
      ranges = feasible;
    }

    return {
      restaurantId: query.restaurantId,
      date: query.dateIso,
      orderType: query.orderType ?? null,
      slotMinutes: policy.slotMinutes,
      maxCapacityUnits: policy.maxCapacityUnits,
      ranges,
    };
  }
}
