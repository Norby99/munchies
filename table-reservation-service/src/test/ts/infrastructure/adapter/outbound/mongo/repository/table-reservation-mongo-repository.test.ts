import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock(
  "@main/infrastructure/adapter/outbound/mongo/document/table-reservation-document",
  () => ({
    TableReservationModel: {
      findById: vi.fn(),
      find: vi.fn(),
      replaceOne: vi.fn(),
    },
  }),
);

import { ReservationId } from "@main/domain/model/ReservationId";
import { TimeSlot } from "@main/domain/model/TimeSlot";
import { TableReservationModel } from "@main/infrastructure/adapter/outbound/mongo/document/table-reservation-document";
import { TableReservationFactory } from "@main/infrastructure/adapter/outbound/mongo/factory/table-reservation-factory";
import { TableReservationMongoRepository } from "@main/infrastructure/adapter/outbound/mongo/repository/table-reservation-mongo-repository";
import { RESTAURANT, at } from "@test/support/fakes";
import { acceptedReservation } from "@test/support/reservations";

const model = vi.mocked(TableReservationModel, true);

/** A mongoose query stub: chainable `sort`, resolved by `lean`. */
function queryReturning(value: unknown) {
  const query = { sort: vi.fn(), lean: vi.fn().mockResolvedValue(value) };
  query.sort.mockReturnValue(query);
  return query;
}

describe("TableReservationMongoRepository", () => {
  const repository = new TableReservationMongoRepository();
  const reservation = acceptedReservation();
  const document = TableReservationFactory.toDocument(reservation);

  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("finds a reservation by id", async () => {
    model.findById.mockReturnValue(queryReturning(document) as never);

    const found = await repository.findById(reservation.id);

    expect(model.findById).toHaveBeenCalledWith(reservation.id.value);
    expect(found?.id.value).toBe(reservation.id.value);
  });

  it("returns null for an unknown id", async () => {
    model.findById.mockReturnValue(queryReturning(null) as never);
    expect(await repository.findById(new ReservationId("missing"))).toBeNull();
  });

  it("lists with the given filters only, ordered by start", async () => {
    const query = queryReturning([document]);
    model.find.mockReturnValue(query as never);

    const found = await repository.find({ customerId: "customer-1", restaurantId: undefined });

    expect(model.find).toHaveBeenCalledWith({ customerId: "customer-1" });
    expect(query.sort).toHaveBeenCalledWith({ start: 1 });
    expect(found).toHaveLength(1);
  });

  it("looks for accepted reservations overlapping the half-open slot", async () => {
    model.find.mockReturnValue(queryReturning([document]) as never);
    const slot = TimeSlot.startingAt(at("20:00"), 120);

    const found = await repository.findAcceptedOverlapping(RESTAURANT, slot);

    expect(model.find).toHaveBeenCalledWith({
      restaurantId: RESTAURANT,
      status: "ACCEPTED",
      start: { $lt: slot.end },
      end: { $gt: slot.start },
    });
    expect(found).toHaveLength(1);
  });

  it("upserts the reservation document", async () => {
    model.replaceOne.mockResolvedValue({} as never);

    expect(await repository.save(reservation)).toBe("SAVED");
    expect(model.replaceOne).toHaveBeenCalledWith({ _id: document._id }, document, {
      upsert: true,
    });
  });

  it("reports the table as taken on a duplicate-key error", async () => {
    model.replaceOne.mockRejectedValue(Object.assign(new Error("E11000"), { code: 11000 }));
    expect(await repository.save(reservation)).toBe("TABLE_TAKEN");
  });

  it("lets any other storage error through", async () => {
    model.replaceOne.mockRejectedValue(new Error("connection lost"));
    await expect(repository.save(reservation)).rejects.toThrow("connection lost");
  });
});
