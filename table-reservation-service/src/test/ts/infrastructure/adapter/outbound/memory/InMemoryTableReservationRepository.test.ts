import { beforeEach, describe, expect, it } from "vitest";
import { ReservationId } from "@main/domain/model/ReservationId";
import { TimeSlot } from "@main/domain/model/TimeSlot";
import { InMemoryTableReservationRepository } from "@main/infrastructure/adapter/outbound/memory/InMemoryTableReservationRepository";
import { at } from "@test/support/fakes";
import { acceptedReservation } from "@test/support/reservations";

describe("InMemoryTableReservationRepository", () => {
  let repository: InMemoryTableReservationRepository;

  beforeEach(() => {
    repository = new InMemoryTableReservationRepository();
  });

  it("finds a saved reservation by id", async () => {
    const reservation = acceptedReservation();
    expect(await repository.save(reservation)).toBe("SAVED");
    expect(await repository.findById(reservation.id)).toBe(reservation);
    expect(await repository.findById(new ReservationId("missing"))).toBeNull();
  });

  it("refuses a second reservation of the same table in an overlapping slot", async () => {
    await repository.save(acceptedReservation({ time: "19:00" }));
    const overlapping = acceptedReservation({ time: "20:00" });

    expect(await repository.save(overlapping)).toBe("TABLE_TAKEN");
    expect(await repository.findById(overlapping.id)).toBeNull();
  });

  it("accepts the same table back to back, another table, or another restaurant", async () => {
    await repository.save(acceptedReservation({ time: "19:00" }));
    expect(await repository.save(acceptedReservation({ time: "21:00" }))).toBe("SAVED");
    expect(await repository.save(acceptedReservation({ tableNumber: 2 }))).toBe("SAVED");
    expect(await repository.save(acceptedReservation({ restaurantId: "other" }))).toBe("SAVED");
  });

  it("replaces a reservation with its new version", async () => {
    const original = acceptedReservation({ time: "19:00" });
    await repository.save(original);
    const moved = original.modify(TimeSlot.startingAt(at("19:30"), 120), 2, 1);

    expect(await repository.save(moved)).toBe("SAVED");
    expect(await repository.findById(original.id)).toBe(moved);
  });

  it("frees the table of a canceled reservation", async () => {
    const original = acceptedReservation();
    await repository.save(original);
    await repository.save(original.cancel());

    expect(await repository.save(acceptedReservation())).toBe("SAVED");
  });

  it("finds the accepted reservations overlapping a slot", async () => {
    const dinner = acceptedReservation({ time: "19:00", tableNumber: 1 });
    const late = acceptedReservation({ time: "21:00", tableNumber: 1 });
    const canceled = acceptedReservation({ time: "19:00", tableNumber: 2 });
    await repository.save(dinner);
    await repository.save(late);
    await repository.save(canceled);
    await repository.save(canceled.cancel());

    const found = await repository.findAcceptedOverlapping(
      dinner.restaurantId,
      TimeSlot.startingAt(at("20:00"), 60),
    );

    expect(found).toEqual([dinner]);
  });

  it("lists by customer and restaurant, earliest first", async () => {
    const late = acceptedReservation({ time: "21:00" });
    const early = acceptedReservation({ time: "19:00" });
    const someoneElse = acceptedReservation({ tableNumber: 2, customerId: "customer-2" });
    const elsewhere = acceptedReservation({ restaurantId: "other" });
    for (const reservation of [late, early, someoneElse, elsewhere]) {
      await repository.save(reservation);
    }

    expect(await repository.find({ customerId: early.customerId })).toEqual([
      early,
      elsewhere,
      late,
    ]);
    expect(await repository.find({ restaurantId: "other" })).toEqual([elsewhere]);
    expect(
      await repository.find({ customerId: early.customerId, restaurantId: early.restaurantId }),
    ).toEqual([early, late]);
  });
});
