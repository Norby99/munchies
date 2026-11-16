import { BookingRepository } from "../domain/ports/bookingRepository";
import { OrderClient, OrderServiceError } from "../domain/ports/orderClient";
import { Booking } from "../domain/model/booking";
import { Trip, TripRoute } from "../domain/model/trip";
import { departTrip, routeFromLegs, routeWaypoints, straightRoute } from "../domain/model/tripProgress";
import { RoutePlanner } from "../domain/ports/routePlanner";
import { TripRepository } from "../domain/ports/tripRepository";
import { AppError, notFound, upstream } from "./errors";

export interface AdvanceResult {
  orderId: string;
  bookingId: string | null;
  bookingStatus: string | null;
}

/**
 * Manager-triggered "move this order to the next state" — forwarded to order-service
 * and reflected on the booking. The internal timer uses the same order-service call
 * for the automated CONFIRMED -> PREPARING step.
 *
 * When the advance puts a delivery order ON_THE_WAY, the driver of its trip leaves the
 * restaurant along a route planned on the streets: from then on DriverSimulator moves it and
 * GetOrderTracking can show it.
 */
export class AdvanceScheduledOrder {
  constructor(
    private readonly bookings: BookingRepository,
    private readonly orders: OrderClient,
    private readonly trips: TripRepository,
    private readonly routes: RoutePlanner,
    private readonly now: () => number = () => Date.now(),
  ) {}

  async execute(orderId: string): Promise<AdvanceResult> {
    if (!orderId) throw notFound("orderId is required");

    try {
      await this.orders.advance(orderId);
    } catch (err) {
      if (err instanceof OrderServiceError) {
        throw new AppError(
          `order-service could not advance the order: ${err.message}`,
          err.status >= 400 && err.status < 500 ? err.status : 502,
        );
      }
      throw upstream(`order-service unreachable: ${err instanceof Error ? err.message : err}`);
    }

    const booking = await this.bookings.findByOrderId(orderId);
    if (booking && booking.status === "CONFIRMED") {
      booking.status = "PREPARING";
      booking.updatedAtEpochMs = this.now();
      await this.bookings.update(booking);
    }

    if (booking?.delivery) await this.departIfOnTheWay(orderId, booking);

    return {
      orderId,
      bookingId: booking?.id ?? null,
      bookingStatus: booking?.status ?? null,
    };
  }

  /**
   * order-service does not say which state the advance reached, so it is read back. A trip
   * leaves once, with the first of its orders that goes ON_THE_WAY. The order is already
   * advanced at this point: a failure here only costs the tracking, so it is logged, not thrown.
   */
  private async departIfOnTheWay(orderId: string, booking: Booking): Promise<void> {
    if (!booking.delivery) return;
    try {
      if ((await this.orders.getStatus(orderId)) !== "ON_THE_WAY") return;
      const trip = await this.trips.findById(booking.delivery.tripId);
      if (!trip || trip.progress) return;
      trip.progress = departTrip(trip, this.now(), await this.planRoute(trip));
      await this.trips.update(trip);
      console.info(`[drivers] trip ${trip.id} left the restaurant with order ${orderId}`);
    } catch (err) {
      console.warn(
        `[drivers] could not start the trip of order ${orderId}:`,
        err instanceof Error ? err.message : err,
      );
    }
  }

  /** A routing engine that is down must not keep the driver at the restaurant. */
  private async planRoute(trip: Trip): Promise<TripRoute> {
    try {
      return routeFromLegs(await this.routes.planLegs(routeWaypoints(trip)));
    } catch (err) {
      console.warn(
        `[drivers] no road route for trip ${trip.id}, using straight lines:`,
        err instanceof Error ? err.message : err,
      );
      return straightRoute(trip);
    }
  }
}
