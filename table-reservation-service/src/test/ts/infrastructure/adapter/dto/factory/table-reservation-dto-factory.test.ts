import { describe, expect, it } from "vitest";
import {
  ModifyTableReservationRequest,
  ReservationStatus,
  ReserveTableRequest,
} from "munchies-table-reservation-service-shared/kotlin/table-reservation-modules";
import { TableReservationDtoFactory } from "@main/infrastructure/adapter/dto/factory/table-reservation-dto-factory";
import { CUSTOMER, RESTAURANT, at } from "@test/support/fakes";
import { acceptedReservation } from "@test/support/reservations";

describe("TableReservationDtoFactory", () => {
  it("maps a reserve request to its command", () => {
    const request = new ReserveTableRequest(RESTAURANT, CUSTOMER, "2026-11-20T19:00:00Z", 3);

    expect(TableReservationDtoFactory.toReserveCommand(request)).toEqual({
      restaurantId: RESTAURANT,
      customerId: CUSTOMER,
      start: at("19:00"),
      numberOfGuests: 3,
    });
  });

  it("maps an unparsable start to an invalid date, left to the use case to refuse", () => {
    const request = new ReserveTableRequest(RESTAURANT, CUSTOMER, "tomorrow evening", 3);
    const command = TableReservationDtoFactory.toReserveCommand(request);
    expect(Number.isNaN(command.start.getTime())).toBe(true);
  });

  it("maps a modify request to its command", () => {
    const request = new ModifyTableReservationRequest(CUSTOMER, "2026-11-20T20:30:00Z", 5);

    expect(TableReservationDtoFactory.toModifyCommand("reservation-1", request)).toEqual({
      reservationId: "reservation-1",
      customerId: CUSTOMER,
      start: at("20:30"),
      numberOfGuests: 5,
    });
  });

  it("maps a reservation to its wire form", () => {
    const reservation = acceptedReservation({ tableNumber: 3, guests: 4 });
    const dto = TableReservationDtoFactory.toDto(reservation);

    expect(dto.id).toBe(reservation.id.value);
    expect(dto.restaurantId).toBe(RESTAURANT);
    expect(dto.customerId).toBe(CUSTOMER);
    expect(dto.start).toBe("2026-11-20T19:00:00.000Z");
    expect(dto.end).toBe("2026-11-20T21:00:00.000Z");
    expect(dto.numberOfGuests).toBe(4);
    expect(dto.tableNumber).toBe(3);
    expect(dto.status).toBe(ReservationStatus.ACCEPTED);
  });
});
