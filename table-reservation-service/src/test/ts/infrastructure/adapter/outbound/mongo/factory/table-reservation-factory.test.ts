import { describe, expect, it } from "vitest";
import { ReservationStatus } from "munchies-table-reservation-service-shared/kotlin/table-reservation-modules";
import { TableReservation } from "@main/domain/model/TableReservation";
import { TimeSlot } from "@main/domain/model/TimeSlot";
import { TableReservationFactory } from "@main/infrastructure/adapter/outbound/mongo/factory/table-reservation-factory";
import { CUSTOMER, RESTAURANT, at } from "@test/support/fakes";
import { acceptedReservation } from "@test/support/reservations";

describe("TableReservationFactory", () => {
  it("maps an accepted reservation to its document", () => {
    const reservation = acceptedReservation({ time: "19:00", tableNumber: 3, guests: 4 });

    expect(TableReservationFactory.toDocument(reservation)).toMatchObject({
      _id: reservation.id.value,
      restaurantId: RESTAURANT,
      customerId: CUSTOMER,
      start: at("19:00"),
      end: at("21:00"),
      numberOfGuests: 4,
      tableNumber: 3,
      status: "ACCEPTED",
    });
  });

  it("round-trips a reservation through its document", () => {
    const reservation = acceptedReservation({ tableNumber: 3 });
    const restored = TableReservationFactory.toDomain(
      TableReservationFactory.toDocument(reservation),
    );

    expect(restored.id.value).toBe(reservation.id.value);
    expect(restored.slot.equals(reservation.slot)).toBe(true);
    expect(restored.status).toBe(ReservationStatus.ACCEPTED);
    expect(restored.tableNumber).toBe(3);
    expect(restored.numberOfGuests).toBe(reservation.numberOfGuests);
  });

  it("round-trips a canceled reservation", () => {
    const canceled = acceptedReservation().cancel();
    const restored = TableReservationFactory.toDomain(
      TableReservationFactory.toDocument(canceled),
    );
    expect(restored.status).toBe(ReservationStatus.CANCELED);
  });

  it("holds its table for every grid step of the slot", () => {
    const reservation = TableReservation.request(
      RESTAURANT,
      CUSTOMER,
      TimeSlot.startingAt(at("19:00"), 45),
      2,
    ).accept(7);

    expect(TableReservationFactory.occupancyOf(reservation)).toEqual([
      "7@2026-11-20T19:00:00.000Z",
      "7@2026-11-20T19:15:00.000Z",
      "7@2026-11-20T19:30:00.000Z",
    ]);
  });

  it("shares a key between overlapping reservations of the same table only", () => {
    const keys = (time: string, tableNumber = 1) =>
      TableReservationFactory.occupancyOf(acceptedReservation({ time, tableNumber }));
    const shared = (a: string[], b: string[]) => a.some((key) => b.includes(key));

    expect(shared(keys("19:00"), keys("20:45"))).toBe(true);
    expect(shared(keys("19:00"), keys("21:00"))).toBe(false);
    expect(shared(keys("19:00"), keys("19:00", 2))).toBe(false);
  });

  it("holds nothing once canceled", () => {
    const canceled = acceptedReservation().cancel();
    expect(TableReservationFactory.toDocument(canceled).occupancy).toEqual([]);
  });
});
