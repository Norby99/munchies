import { randomUUID } from "node:crypto";
import { isBookableSlot } from "../domain/model/availability";
import { Booking, DeliveryBookingInfo, OrderType } from "../domain/model/booking";
import { GeoPoint, isValidGeoPoint } from "../domain/model/geo";
import { planDeliveryPlacement, Trip, TripPlan } from "../domain/model/trip";
import { CapacityPolicy } from "../domain/model/capacityPolicy";
import { BookingRepository } from "../domain/ports/bookingRepository";
import { OrderClient, OrderItem, OrderServiceError, PlaceOrderInput } from "../domain/ports/orderClient";
import { TripRepository } from "../domain/ports/tripRepository";
import { PolicyResolver } from "./policyResolver";
import { DeliveryGate } from "./deliveryGate";
import { AppError, badRequest, conflict, upstream } from "./errors";

export interface ScheduleOrderInput {
  restaurantId: string;
  customerId: string;
  items: OrderItem[];
  orderType: OrderType;
  slotStartEpochMs: number;
  // DELIVERY
  deliveryAddress?: string;
  deliveryLocation?: GeoPoint;
  bellName?: string;
  customerPhone?: string;
  // TAKEAWAY
  customerName?: string;
  // DINE_IN
  tableNumber?: number;
  numberOfGuests?: number;
}

export interface ScheduleOrderResult {
  bookingId: string;
  orderId: string;
  restaurantId: string;
  orderType: OrderType;
  slotStartEpochMs: number;
  prepareAtEpochMs: number;
  status: Booking["status"];
  tripId: string | null;
}

const ORDER_TYPES: OrderType[] = ["DELIVERY", "TAKEAWAY", "DINE_IN"];

/**
 * Confirms a scheduled order: checks kitchen weight capacity, plans the delivery trip
 * (DELIVERY only), places the order in order-service, then records the booking and
 * the trip change. The trip is only changed after order placement succeeds.
 */
export class ScheduleOrder {
  constructor(
    private readonly policies: PolicyResolver,
    private readonly bookings: BookingRepository,
    private readonly trips: TripRepository,
    private readonly orders: OrderClient,
    private readonly delivery: DeliveryGate,
    private readonly now: () => number = () => Date.now(),
  ) {}

  async execute(input: ScheduleOrderInput): Promise<ScheduleOrderResult> {
    this.validateShape(input);
    let policy: CapacityPolicy;
    let origin: GeoPoint | null = null;
    if (input.orderType === "DELIVERY") {
      const setup = await this.delivery.resolve(input.restaurantId);
      if (!setup) {
        throw conflict(
          "deliveries are not available: the restaurant has not set its location and capacity policy",
        );
      }
      policy = setup.policy;
      origin = setup.origin;
    } else {
      policy = await this.policies.resolve(input.restaurantId);
    }

    if (!isBookableSlot(input.slotStartEpochMs, policy, this.now())) {
      throw badRequest(
        "slotStartEpochMs is not a bookable slot for this restaurant (wrong grain, in the past, " +
          "inside the prep lead time, or beyond the max-advance window)",
      );
    }

    const weight = input.items.reduce((sum, it) => sum + it.quantity, 0);
    const usedUnits =
      (await this.bookings.sumActiveWeightBySlot(input.restaurantId, [input.slotStartEpochMs])).get(
        input.slotStartEpochMs,
      ) ?? 0;
    if (usedUnits + weight > policy.maxCapacityUnits) {
      throw conflict("That time slot is full — ask for availability again and pick another hour");
    }

    const bookingId = randomUUID();
    const newTripId = randomUUID();
    let plan: TripPlan | null = null;
    let delivery: DeliveryBookingInfo | null = null;
    if (input.orderType === "DELIVERY") {
      const point = input.deliveryLocation as GeoPoint;
      const trips = await this.trips.findBySlot(input.restaurantId, input.slotStartEpochMs);
      plan = planDeliveryPlacement(origin as GeoPoint, trips, point, policy);
      if (plan.kind === "rejected") {
        throw plan.reason === "too_far"
          ? badRequest("this delivery is too far for a single trip within the restaurant's km budget")
          : conflict("no delivery trip is left in this slot — pick another hour");
      }
      delivery = { location: point, tripId: plan.kind === "join" ? plan.trip.id : newTripId };
    }

    const nowMs = this.now();
    const booking: Booking = {
      id: bookingId,
      restaurantId: input.restaurantId,
      customerId: input.customerId,
      orderId: null,
      orderType: input.orderType,
      slotStartEpochMs: input.slotStartEpochMs,
      prepareAtEpochMs: input.slotStartEpochMs - policy.prepLeadMinutes * 60_000,
      status: "CONFIRMED",
      weight,
      delivery,
      createdAtEpochMs: nowMs,
      updatedAtEpochMs: nowMs,
    };
    await this.bookings.save(booking); // reserve before the remote call

    let placed;
    try {
      placed = await this.orders.place(this.toPlaceOrderInput(input));
    } catch (err) {
      booking.status = "FAILED";
      booking.updatedAtEpochMs = this.now();
      await this.bookings.update(booking);
      if (err instanceof OrderServiceError) {
        throw new AppError(
          `order-service rejected the order: ${err.message}`,
          err.status >= 400 && err.status < 500 ? err.status : 502,
        );
      }
      throw upstream(`order-service unreachable: ${err instanceof Error ? err.message : err}`);
    }

    booking.orderId = placed.orderId;
    booking.updatedAtEpochMs = this.now();
    await this.bookings.update(booking);
    if (plan && delivery) await this.applyTripPlan(plan, delivery, bookingId, input, newTripId);

    return {
      bookingId,
      orderId: placed.orderId,
      restaurantId: booking.restaurantId,
      orderType: booking.orderType,
      slotStartEpochMs: booking.slotStartEpochMs,
      prepareAtEpochMs: booking.prepareAtEpochMs,
      status: booking.status,
      tripId: delivery?.tripId ?? null,
    };
  }

  private async applyTripPlan(
    plan: TripPlan,
    delivery: DeliveryBookingInfo,
    bookingId: string,
    input: ScheduleOrderInput,
    newTripId: string,
  ): Promise<void> {
    if (plan.kind === "join") {
      const trip: Trip = plan.trip;
      trip.stops.splice(plan.position, 0, { bookingId, location: delivery.location });
      trip.totalKm = plan.newTotalKm;
      await this.trips.update(trip);
    } else if (plan.kind === "new") {
      await this.trips.save({
        id: newTripId,
        restaurantId: input.restaurantId,
        slotStartEpochMs: input.slotStartEpochMs,
        stops: [{ bookingId, location: delivery.location }],
        totalKm: plan.costKm,
      });
    }
  }

  private validateShape(input: ScheduleOrderInput): void {
    if (!input.restaurantId) throw badRequest("restaurantId is required");
    if (!input.customerId) throw badRequest("customerId is required (injected by the gateway)");
    if (!ORDER_TYPES.includes(input.orderType)) {
      throw badRequest(`orderType must be one of ${ORDER_TYPES.join(", ")}`);
    }
    if (!Array.isArray(input.items) || input.items.length === 0) {
      throw badRequest("items must be a non-empty array");
    }
    if (input.items.some((it) => !it.menuItemId || !Number.isInteger(it.quantity) || it.quantity <= 0)) {
      throw badRequest("every item needs a menuItemId and an integer quantity > 0");
    }
    if (!Number.isFinite(input.slotStartEpochMs)) {
      throw badRequest("slotStartEpochMs must be an epoch-millisecond number");
    }
    if (input.orderType === "DELIVERY") {
      if (!input.deliveryAddress) throw badRequest("deliveryAddress is required for DELIVERY orders");
      if (!isValidGeoPoint(input.deliveryLocation)) {
        throw badRequest(
          "deliveryLocation {lat, lng} is required for DELIVERY orders (finite numbers, lat in [-90,90], lng in [-180,180])",
        );
      }
    }
    if (input.orderType === "DINE_IN") {
      if (!Number.isInteger(input.tableNumber) || !Number.isInteger(input.numberOfGuests)) {
        throw badRequest("tableNumber and numberOfGuests are required for DINE_IN orders");
      }
    }
  }

  private toPlaceOrderInput(input: ScheduleOrderInput): PlaceOrderInput {
    const slotAsString = String(input.slotStartEpochMs);
    const base: PlaceOrderInput = {
      restaurantId: input.restaurantId,
      customerId: input.customerId,
      items: input.items,
      orderType: input.orderType,
    };
    switch (input.orderType) {
      case "DELIVERY":
        return {
          ...base,
          estimatedDeliveryTime: slotAsString,
          deliveryAddress: input.deliveryAddress,
          bellName: input.bellName ?? "",
          customerPhone: input.customerPhone ?? "",
        };
      case "TAKEAWAY":
        return { ...base, pickupTime: slotAsString, customerName: input.customerName ?? "" };
      case "DINE_IN":
        return { ...base, tableNumber: input.tableNumber, numberOfGuests: input.numberOfGuests };
    }
  }
}
