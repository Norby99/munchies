import {
  ModifyTableReservationRequest,
  ReserveTableRequest,
  TableReservationDto,
} from "munchies-table-reservation-service-shared/kotlin/table-reservation-modules";
import { ModifyTableReservationCommand } from "@main/application/port/inbound/command/ModifyTableReservationCommand";
import { ReserveTableCommand } from "@main/application/port/inbound/command/ReserveTableCommand";
import { TableReservation } from "@main/domain/model/TableReservation";

/** Mapping between the wire contracts of table-reservation-shared and commands / the domain. */
export class TableReservationDtoFactory {
  /**
   * An unparsable `start` becomes an invalid Date rather than an error here: judging the input
   * is the use case's job, which refuses it like any other invalid start.
   */
  static toReserveCommand(request: ReserveTableRequest): ReserveTableCommand {
    return {
      restaurantId: request.restaurantId,
      customerId: request.customerId,
      start: new Date(request.start),
      numberOfGuests: request.numberOfGuests,
    };
  }

  static toModifyCommand(
    reservationId: string,
    request: ModifyTableReservationRequest,
  ): ModifyTableReservationCommand {
    return {
      reservationId,
      customerId: request.customerId,
      start: new Date(request.start),
      numberOfGuests: request.numberOfGuests,
    };
  }

  static toDto(reservation: TableReservation): TableReservationDto {
    return new TableReservationDto(
      reservation.id.value,
      reservation.restaurantId,
      reservation.customerId,
      reservation.slot.start.toISOString(),
      reservation.slot.end.toISOString(),
      reservation.numberOfGuests,
      // A stored reservation always has a table; 0 only for one never accepted.
      reservation.tableNumber ?? 0,
      reservation.status,
    );
  }
}
