import { describe, expect, it } from "vitest";
import { ReservationStatus } from "munchies-table-reservation-service-shared/kotlin/table-reservation-modules";
import { InvalidReservationError } from "@main/domain/model/InvalidReservationError";
import { TableReservation } from "@main/domain/model/TableReservation";
import { TimeSlot } from "@main/domain/model/TimeSlot";
import { CUSTOMER, RESTAURANT, at } from "@test/support/fakes";

const dinner = TimeSlot.startingAt(at("19:00"), 120);
const requested = () => TableReservation.request(RESTAURANT, CUSTOMER, dinner, 2);

describe("TableReservation", () => {
  it("is requested without a table", () => {
    const reservation = requested();
    expect(reservation.status).toBe(ReservationStatus.REQUESTED);
    expect(reservation.tableNumber).toBeNull();
    expect(reservation.isAccepted).toBe(false);
  });

  it("gets a fresh id on every request", () => {
    expect(requested().id.value).not.toBe(requested().id.value);
  });

  it.each([0, -2, 2.5])("cannot be requested for %s guests", (guests) => {
    expect(() => TableReservation.request(RESTAURANT, CUSTOMER, dinner, guests)).toThrow(
      InvalidReservationError,
    );
  });

  it("cannot be requested without a restaurant or a customer", () => {
    expect(() => TableReservation.request(" ", CUSTOMER, dinner, 2)).toThrow("restaurant");
    expect(() => TableReservation.request(RESTAURANT, "", dinner, 2)).toThrow("customer");
  });

  it("is accepted with a table, leaving the request untouched", () => {
    const request = requested();
    const accepted = request.accept(5);
    expect(accepted.status).toBe(ReservationStatus.ACCEPTED);
    expect(accepted.tableNumber).toBe(5);
    expect(accepted.id.value).toBe(request.id.value);
    expect(request.status).toBe(ReservationStatus.REQUESTED);
  });

  it("cannot be accepted twice", () => {
    expect(() => requested().accept(5).accept(6)).toThrow(InvalidReservationError);
  });

  it("is modified to a new slot, party size and table", () => {
    const later = TimeSlot.startingAt(at("20:00"), 120);
    const modified = requested().accept(5).modify(later, 4, 7);
    expect(modified.slot.equals(later)).toBe(true);
    expect(modified.numberOfGuests).toBe(4);
    expect(modified.tableNumber).toBe(7);
    expect(modified.status).toBe(ReservationStatus.ACCEPTED);
  });

  it("cannot be modified to no guests", () => {
    expect(() => requested().accept(5).modify(dinner, 0, 5)).toThrow(InvalidReservationError);
  });

  it("cannot be modified unless accepted", () => {
    expect(() => requested().modify(dinner, 2, 5)).toThrow(InvalidReservationError);
    expect(() => requested().accept(5).cancel().modify(dinner, 2, 5)).toThrow(
      InvalidReservationError,
    );
  });

  it("is canceled, keeping what was booked", () => {
    const canceled = requested().accept(5).cancel();
    expect(canceled.status).toBe(ReservationStatus.CANCELED);
    expect(canceled.tableNumber).toBe(5);
  });

  it("cannot be canceled twice, nor before being accepted", () => {
    expect(() => requested().accept(5).cancel().cancel()).toThrow(InvalidReservationError);
    expect(() => requested().cancel()).toThrow(InvalidReservationError);
  });

  it("belongs to the customer who requested it", () => {
    expect(requested().isOwnedBy(CUSTOMER)).toBe(true);
    expect(requested().isOwnedBy("someone-else")).toBe(false);
  });

  it("occupies its table during its slot only while accepted", () => {
    const accepted = requested().accept(5);
    const overlapping = TimeSlot.startingAt(at("20:00"), 120);
    const afterwards = TimeSlot.startingAt(at("21:00"), 120);
    expect(accepted.occupies(5, overlapping)).toBe(true);
    expect(accepted.occupies(6, overlapping)).toBe(false);
    expect(accepted.occupies(5, afterwards)).toBe(false);
    expect(accepted.cancel().occupies(5, overlapping)).toBe(false);
    expect(requested().occupies(5, overlapping)).toBe(false);
  });

  it("has started once its slot begins", () => {
    expect(requested().hasStartedBy(at("18:59"))).toBe(false);
    expect(requested().hasStartedBy(at("19:00"))).toBe(true);
  });
});
