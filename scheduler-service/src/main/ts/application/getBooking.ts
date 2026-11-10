import { Booking } from "../domain/model/booking";
import { BookingRepository } from "../domain/ports/bookingRepository";

/** Single booking, including the delivery trip it was grouped into (if any). */
export class GetBooking {
  constructor(private readonly bookings: BookingRepository) {}

  async execute(bookingId: string): Promise<Booking | null> {
    return this.bookings.findById(bookingId);
  }
}
