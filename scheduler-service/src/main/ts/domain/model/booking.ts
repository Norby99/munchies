import { GeoPoint } from "./geo";

export type OrderType = "DELIVERY" | "TAKEAWAY" | "DINE_IN";

export type BookingStatus =
  | "CONFIRMED" // order placed in order-service, waiting for its prep time
  | "PREPARING" // scheduler has advanced the order to PREPARING
  | "CANCELLED"
  | "FAILED"; // order placement failed; slot released

export interface DeliveryBookingInfo {
  location: GeoPoint;
  /** Internal trip grouping; no customer-facing meaning. */
  tripId: string;
}

export interface Booking {
  id: string;
  restaurantId: string;
  customerId: string;
  orderId: string | null;
  orderType: OrderType;
  /** Epoch ms of the slot the customer picked (pickup / delivery target time). */
  slotStartEpochMs: number;
  /** Epoch ms at which the scheduler should tell order-service to start preparing. */
  prepareAtEpochMs: number;
  status: BookingStatus;
  /** Kitchen capacity units this order consumes: sum of item quantities. */
  weight: number;
  /** Set only for DELIVERY orders. */
  delivery: DeliveryBookingInfo | null;
  createdAtEpochMs: number;
  updatedAtEpochMs: number;
}

export const ACTIVE_BOOKING_STATUSES: BookingStatus[] = ["CONFIRMED", "PREPARING"];

export function isActive(booking: Booking): boolean {
  return ACTIVE_BOOKING_STATUSES.includes(booking.status);
}
