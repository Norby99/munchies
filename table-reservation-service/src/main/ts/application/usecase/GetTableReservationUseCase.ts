import { GetTableReservation } from "@main/application/port/inbound/GetTableReservation";
import { ReservationResult } from "@main/application/port/inbound/ReservationResult";
import { ReservationId } from "@main/domain/model/ReservationId";
import { TableReservationRepository } from "@main/domain/port/table-reservation-repository";

/** Use case reading one reservation, optionally only if it belongs to a given customer. */
export class GetTableReservationUseCase implements GetTableReservation {
  constructor(private readonly repository: TableReservationRepository) {}

  async execute(reservationId: string, customerId?: string): Promise<ReservationResult> {
    const reservation = await this.repository.findById(new ReservationId(reservationId));
    if (!reservation || (customerId !== undefined && !reservation.isOwnedBy(customerId))) {
      return { type: "RESERVATION_NOT_FOUND", reason: "Reservation not found" };
    }
    return { type: "SUCCESS", reservation };
  }
}
