import mongoose from "mongoose";
import { afterAll, beforeAll, beforeEach, describe, expect, it } from "vitest";
import { ReservationStatus } from "munchies-table-reservation-service-shared/kotlin/table-reservation-modules";
import { TimeSlot } from "@main/domain/model/TimeSlot";
import { TableReservationModel } from "@main/infrastructure/adapter/outbound/mongo/document/table-reservation-document";
import { TableReservationMongoRepository } from "@main/infrastructure/adapter/outbound/mongo/repository/table-reservation-mongo-repository";
import { RESTAURANT, at } from "@test/support/fakes";
import { acceptedReservation } from "@test/support/reservations";

/**
 * Integration test of the MongoDB adapter against a real database, named by
 * `MONGODB_TEST_URI` (e.g. the one of `config/mongodb/docker-compose.yml`). Its point is the
 * guard against double booking, which only a real unique index can prove.
 */
const uri = process.env.MONGODB_TEST_URI;

describe.skipIf(!uri)("TableReservationMongoRepository against MongoDB", () => {
  const repository = new TableReservationMongoRepository();

  beforeAll(async () => {
    await mongoose.connect(uri!);
    await TableReservationModel.init();
  });

  beforeEach(async () => {
    await TableReservationModel.deleteMany({});
  });

  afterAll(async () => {
    await mongoose.connection.dropDatabase();
    await mongoose.disconnect();
  });

  it("stores a reservation and reads it back", async () => {
    const reservation = acceptedReservation({ tableNumber: 3, guests: 4 });

    expect(await repository.save(reservation)).toBe("SAVED");

    const found = await repository.findById(reservation.id);
    expect(found?.id.value).toBe(reservation.id.value);
    expect(found?.slot.equals(reservation.slot)).toBe(true);
    expect(found?.tableNumber).toBe(3);
    expect(found?.numberOfGuests).toBe(4);
    expect(found?.status).toBe(ReservationStatus.ACCEPTED);
  });

  it("refuses the same table in an overlapping slot", async () => {
    await repository.save(acceptedReservation({ time: "19:00" }));
    const overlapping = acceptedReservation({ time: "20:45", customerId: "customer-2" });

    expect(await repository.save(overlapping)).toBe("TABLE_TAKEN");
    expect(await repository.findById(overlapping.id)).toBeNull();
  });

  it("accepts the same table back to back, another table, or another restaurant", async () => {
    await repository.save(acceptedReservation({ time: "19:00" }));

    expect(await repository.save(acceptedReservation({ time: "21:00" }))).toBe("SAVED");
    expect(await repository.save(acceptedReservation({ time: "17:00" }))).toBe("SAVED");
    expect(await repository.save(acceptedReservation({ tableNumber: 2 }))).toBe("SAVED");
    expect(await repository.save(acceptedReservation({ restaurantId: "other" }))).toBe("SAVED");
  });

  it("lets exactly one of many concurrent bookings of a table through", async () => {
    const attempts = Array.from({ length: 20 }, (_, i) =>
      acceptedReservation({ time: "19:00", customerId: `customer-${i}` }),
    );

    const outcomes = await Promise.all(attempts.map((attempt) => repository.save(attempt)));

    expect(outcomes.filter((outcome) => outcome === "SAVED")).toHaveLength(1);
    expect(await repository.find({ restaurantId: RESTAURANT })).toHaveLength(1);
  });

  it("moves a reservation without tripping over its own table", async () => {
    const original = acceptedReservation({ time: "19:00" });
    await repository.save(original);

    const moved = original.modify(TimeSlot.startingAt(at("19:30"), 120), 2, 1);

    expect(await repository.save(moved)).toBe("SAVED");
    expect((await repository.findById(original.id))?.slot.start).toEqual(at("19:30"));
  });

  it("keeps the stored reservation when moving it onto a taken table fails", async () => {
    const original = acceptedReservation({ time: "19:00", tableNumber: 1 });
    await repository.save(original);
    await repository.save(acceptedReservation({ time: "19:00", tableNumber: 2 }));

    const moved = original.modify(original.slot, 2, 2);

    expect(await repository.save(moved)).toBe("TABLE_TAKEN");
    expect((await repository.findById(original.id))?.tableNumber).toBe(1);
  });

  it("frees the table of a canceled reservation, any number of times", async () => {
    for (const customerId of ["customer-1", "customer-2", "customer-3"]) {
      const reservation = acceptedReservation({ customerId });
      expect(await repository.save(reservation)).toBe("SAVED");
      expect(await repository.save(reservation.cancel())).toBe("SAVED");
    }
    expect(await repository.find({ restaurantId: RESTAURANT })).toHaveLength(3);
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
      RESTAURANT,
      TimeSlot.startingAt(at("20:00"), 60),
    );

    expect(found.map((reservation) => reservation.id.value)).toEqual([dinner.id.value]);
  });

  it("lists by customer and restaurant, earliest first", async () => {
    await repository.save(acceptedReservation({ time: "21:00" }));
    await repository.save(acceptedReservation({ time: "19:00" }));
    await repository.save(acceptedReservation({ tableNumber: 2, customerId: "customer-2" }));

    const own = await repository.find({ customerId: "customer-1" });
    const all = await repository.find({ restaurantId: RESTAURANT });

    expect(own.map((reservation) => reservation.slot.start)).toEqual([at("19:00"), at("21:00")]);
    expect(all).toHaveLength(3);
  });
});
