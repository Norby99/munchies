import { GeoPoint } from "../domain/model/geo";
import { DeliveryTrackingStatus, deliveryProgress } from "../domain/model/tripProgress";
import { BookingRepository } from "../domain/ports/bookingRepository";
import { TripRepository } from "../domain/ports/tripRepository";
import { notFound } from "./errors";

/** Who is asking, as verified by the gateway. Absent on internal calls. */
export interface TrackingViewer {
  id: string;
  role: string;
}

export interface OrderTracking {
  orderId: string;
  tripId: string;
  status: DeliveryTrackingStatus;
  /** The restaurant the driver leaves from. */
  origin: GeoPoint;
  destination: GeoPoint;
  driverPosition: GeoPoint | null;
  remainingKm: number;
  etaEpochMs: number | null;
  stopsBefore: number;
  /** The route still to drive, from the driver to this delivery. Null unless on the way. */
  routeAhead: GeoPoint[] | null;
  speedKmh: number;
  updatedAtEpochMs: number | null;
}

/**
 * Where the driver carrying a delivery order is. Reads the last position stored on the trip;
 * it never moves the driver itself (see DriverSimulator).
 */
export class GetOrderTracking {
  constructor(
    private readonly bookings: BookingRepository,
    private readonly trips: TripRepository,
    private readonly speedKmh: number,
  ) {}

  async execute(orderId: string, viewer?: TrackingViewer): Promise<OrderTracking> {
    const booking = await this.bookings.findByOrderId(orderId);
    // A customer asking for someone else's order gets the same answer as for an unknown one.
    const foreign = viewer?.role === "CUSTOMER" && booking?.customerId !== viewer.id;
    if (!booking || !booking.delivery || foreign) {
      throw notFound("no delivery tracking for this order");
    }
    const trip = await this.trips.findById(booking.delivery.tripId);
    const progress = trip ? deliveryProgress(trip, booking.id, this.speedKmh) : null;
    if (!trip || !progress) throw notFound("no delivery tracking for this order");

    return {
      orderId,
      tripId: trip.id,
      origin: { ...trip.origin },
      speedKmh: this.speedKmh,
      ...progress,
    };
  }
}
