import { BookingRepository } from "../domain/ports/bookingRepository";
import { OrderClient, OrderServiceError } from "../domain/ports/orderClient";
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
 */
export class AdvanceScheduledOrder {
  constructor(
    private readonly bookings: BookingRepository,
    private readonly orders: OrderClient,
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

    return {
      orderId,
      bookingId: booking?.id ?? null,
      bookingStatus: booking?.status ?? null,
    };
  }
}
