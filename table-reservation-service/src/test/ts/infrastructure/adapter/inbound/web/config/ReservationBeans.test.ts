import { afterEach, describe, expect, it } from "vitest";
import { InvalidReservationError } from "@main/domain/model/InvalidReservationError";
import { ReservationBeans } from "@main/infrastructure/adapter/inbound/web/config/ReservationBeans";

describe("ReservationBeans", () => {
  afterEach(() => {
    ReservationBeans.resetInstance();
  });

  it("books two hours unless configured otherwise", () => {
    expect(ReservationBeans.policyFrom({}).durationMinutes).toBe(120);
    expect(ReservationBeans.policyFrom({ RESERVATION_DURATION_MINUTES: " " }).durationMinutes).toBe(
      120,
    );
  });

  it("reads the slot length from the environment", () => {
    expect(ReservationBeans.policyFrom({ RESERVATION_DURATION_MINUTES: "90" }).durationMinutes).toBe(
      90,
    );
  });

  it("refuses an unusable slot length", () => {
    expect(() => ReservationBeans.policyFrom({ RESERVATION_DURATION_MINUTES: "100" })).toThrow(
      InvalidReservationError,
    );
    expect(() => ReservationBeans.policyFrom({ RESERVATION_DURATION_MINUTES: "soon" })).toThrow(
      InvalidReservationError,
    );
  });

  it("wires every inbound port, with or without MongoDB", () => {
    for (const env of [{}, { MONGODB_URI: "mongodb://localhost/test" }]) {
      const services = ReservationBeans.createDefaultServices(env);
      expect(Object.keys(services).sort()).toEqual([
        "cancelTableReservation",
        "getTableReservation",
        "getTableReservations",
        "modifyTableReservation",
        "reserveTable",
      ]);
    }
  });

  it("builds the default services once", () => {
    expect(ReservationBeans.getDefaultServices()).toBe(ReservationBeans.getDefaultServices());
  });
});
