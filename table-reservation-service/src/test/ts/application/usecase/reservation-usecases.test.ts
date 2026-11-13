import { beforeEach, describe, expect, it } from "vitest";
import { ReservationStatus } from "munchies-table-reservation-service-shared/kotlin/table-reservation-modules";
import { ReservationResult } from "@main/application/port/inbound/ReservationResult";
import { ReservationPolicy } from "@main/domain/model/ReservationPolicy";
import { TableReservation } from "@main/domain/model/TableReservation";
import {
  SaveReservationResult,
  TableReservationRepository,
} from "@main/domain/port/table-reservation-repository";
import {
  ReservationBeans,
  ReservationServices,
} from "@main/infrastructure/adapter/inbound/web/config/ReservationBeans";
import { InMemoryTableReservationRepository } from "@main/infrastructure/adapter/outbound/memory/InMemoryTableReservationRepository";
import {
  CUSTOMER,
  FakeRestaurantServiceClient,
  MANAGER,
  NOW,
  RESTAURANT,
  RecordingLogger,
  RecordingPublisher,
  at,
} from "@test/support/fakes";

/** Lets the not-awaited notification publication settle. */
const settle = () => new Promise((resolve) => setImmediate(resolve));

function reservationOf(result: ReservationResult): TableReservation {
  if (result.type !== "SUCCESS") throw new Error(`Expected SUCCESS, got ${result.type}`);
  return result.reservation;
}

describe("reservation use cases", () => {
  let repository: InMemoryTableReservationRepository;
  let restaurants: FakeRestaurantServiceClient;
  let publisher: RecordingPublisher;
  let logger: RecordingLogger;
  let clock: Date;
  let services: ReservationServices;

  const wire = (store: TableReservationRepository = repository) =>
    ReservationBeans.createServices({
      repository: store,
      restaurantServiceClient: restaurants,
      notificationPublisher: publisher,
      logger,
      policy: new ReservationPolicy(120),
      now: () => clock,
    });

  const reserve = (time = "19:00", guests = 2, customerId = CUSTOMER) =>
    services.reserveTable.execute({
      restaurantId: RESTAURANT,
      customerId,
      start: at(time),
      numberOfGuests: guests,
    });

  beforeEach(() => {
    repository = new InMemoryTableReservationRepository();
    // Table 1 seats two, table 2 seats four.
    restaurants = new FakeRestaurantServiceClient();
    publisher = new RecordingPublisher();
    logger = new RecordingLogger();
    clock = NOW;
    services = wire();
  });

  describe("reserve a table", () => {
    it("accepts the reservation at the smallest table that fits", async () => {
      const reservation = reservationOf(await reserve("19:00", 2));

      expect(reservation.status).toBe(ReservationStatus.ACCEPTED);
      expect(reservation.tableNumber).toBe(1);
      expect(reservation.slot.start).toEqual(at("19:00"));
      expect(reservation.slot.end).toEqual(at("21:00"));
      expect(await repository.findById(reservation.id)).toEqual(reservation);
    });

    it("publishes the accepted reservation", async () => {
      const reservation = reservationOf(await reserve());
      expect(publisher.accepted).toEqual([reservation]);
    });

    it("seats a second party at another table in the same slot", async () => {
      await reserve("19:00", 2);
      const second = reservationOf(await reserve("20:00", 2, "customer-2"));
      expect(second.tableNumber).toBe(2);
    });

    it("refuses when every fitting table is taken in the slot", async () => {
      await reserve("19:00", 2);
      await reserve("19:00", 2);
      const result = await reserve("20:45", 2);

      expect(result.type).toBe("NO_TABLE_AVAILABLE");
      expect(await repository.find({ restaurantId: RESTAURANT })).toHaveLength(2);
      expect(publisher.accepted).toHaveLength(2);
    });

    it("books the same table again right after the previous slot ends", async () => {
      await reserve("19:00", 2);
      const next = reservationOf(await reserve("21:00", 2));
      expect(next.tableNumber).toBe(1);
    });

    it("refuses a party larger than any table", async () => {
      const result = await reserve("19:00", 5);
      expect(result).toMatchObject({ type: "NO_TABLE_AVAILABLE" });
    });

    it("refuses when the restaurant has no tables configured", async () => {
      restaurants.tables = [];
      expect((await reserve()).type).toBe("NO_TABLE_AVAILABLE");
    });

    it("reports an unknown restaurant", async () => {
      const result = await services.reserveTable.execute({
        restaurantId: "nowhere",
        customerId: CUSTOMER,
        start: at("19:00"),
        numberOfGuests: 2,
      });
      expect(result.type).toBe("RESTAURANT_NOT_FOUND");
    });

    it("reports restaurant-service being unavailable", async () => {
      restaurants.available = false;
      expect((await reserve()).type).toBe("RESTAURANT_UNAVAILABLE");
    });

    it.each([
      ["a start in the past", { start: at("09:00") }],
      ["a start off the grid", { start: at("19:05") }],
      ["an unparsable start", { start: new Date("whenever") }],
      ["no guests", { numberOfGuests: 0 }],
      ["no customer", { customerId: "" }],
      ["no restaurant", { restaurantId: "" }],
    ])("rejects %s without asking restaurant-service", async (_name, override) => {
      restaurants.available = false;
      const result = await services.reserveTable.execute({
        restaurantId: RESTAURANT,
        customerId: CUSTOMER,
        start: at("19:00"),
        numberOfGuests: 2,
        ...override,
      });
      expect(result.type).toBe("INVALID_REQUEST");
    });

    it("tries the next table when a concurrent request takes the first one", async () => {
      // The table looks free when read, but is taken by the time the reservation is stored.
      class RacingRepository extends InMemoryTableReservationRepository {
        private raced = false;

        override save(reservation: TableReservation): Promise<SaveReservationResult> {
          if (!this.raced) {
            this.raced = true;
            return Promise.resolve("TABLE_TAKEN");
          }
          return super.save(reservation);
        }
      }
      services = wire(new RacingRepository());

      const reservation = reservationOf(await reserve("19:00", 2));
      expect(reservation.tableNumber).toBe(2);
    });

    it("still succeeds when the event cannot be published", async () => {
      publisher.failure = new Error("broker down");

      const result = await reserve();
      await settle();

      expect(result.type).toBe("SUCCESS");
      expect(logger.errors).toEqual(["Failed to publish the reservation-accepted event"]);
    });
  });

  describe("modify a reservation", () => {
    const modify = (id: string, time: string, guests: number, customerId = CUSTOMER) =>
      services.modifyTableReservation.execute({
        reservationId: id,
        customerId,
        start: at(time),
        numberOfGuests: guests,
      });

    it("moves the reservation to another time, keeping its table", async () => {
      const original = reservationOf(await reserve("19:00", 2));

      const modified = reservationOf(await modify(original.id.value, "20:00", 2));

      expect(modified.id.value).toBe(original.id.value);
      expect(modified.slot.start).toEqual(at("20:00"));
      expect(modified.tableNumber).toBe(1);
      expect(await repository.findById(original.id)).toEqual(modified);
      expect(publisher.modified).toEqual([modified]);
    });

    it("does not count the reservation's own table as taken", async () => {
      const original = reservationOf(await reserve("19:00", 2));
      await reserve("19:00", 4, "customer-2");

      const modified = reservationOf(await modify(original.id.value, "19:30", 2));

      expect(modified.tableNumber).toBe(1);
    });

    it("moves to a larger table when the party grows", async () => {
      const original = reservationOf(await reserve("19:00", 2));
      const modified = reservationOf(await modify(original.id.value, "19:00", 4));
      expect(modified.tableNumber).toBe(2);
    });

    it("leaves the reservation untouched when no table fits the change", async () => {
      const original = reservationOf(await reserve("19:00", 2));
      await reserve("19:00", 4, "customer-2");

      const result = await modify(original.id.value, "19:00", 4);

      expect(result.type).toBe("NO_TABLE_AVAILABLE");
      expect(await repository.findById(original.id)).toEqual(original);
      expect(publisher.modified).toEqual([]);
    });

    it("rejects an invalid change", async () => {
      const original = reservationOf(await reserve());
      expect((await modify(original.id.value, "19:05", 2)).type).toBe("INVALID_REQUEST");
      expect((await modify(original.id.value, "20:00", 0)).type).toBe("INVALID_REQUEST");
    });

    it("refuses another customer", async () => {
      const original = reservationOf(await reserve());
      const result = await modify(original.id.value, "20:00", 2, "someone-else");
      expect(result.type).toBe("FORBIDDEN");
    });

    it("reports an unknown reservation", async () => {
      expect((await modify("missing", "20:00", 2)).type).toBe("RESERVATION_NOT_FOUND");
    });

    it("refuses a canceled reservation", async () => {
      const original = reservationOf(await reserve());
      await services.cancelTableReservation.execute(original.id.value, CUSTOMER);
      expect((await modify(original.id.value, "20:00", 2)).type).toBe("NOT_CHANGEABLE");
    });

    it("refuses a reservation that has already started", async () => {
      const original = reservationOf(await reserve("19:00"));
      clock = at("19:30");
      expect((await modify(original.id.value, "22:00", 2)).type).toBe("NOT_CHANGEABLE");
    });
  });

  describe("cancel a reservation", () => {
    it("cancels the reservation and frees its table", async () => {
      const original = reservationOf(await reserve("19:00", 2));

      const canceled = reservationOf(
        await services.cancelTableReservation.execute(original.id.value, CUSTOMER),
      );

      expect(canceled.status).toBe(ReservationStatus.CANCELED);
      expect(publisher.canceled).toEqual([canceled]);
      expect(reservationOf(await reserve("19:00", 2, "customer-2")).tableNumber).toBe(1);
    });

    it("refuses another customer", async () => {
      const original = reservationOf(await reserve());
      const result = await services.cancelTableReservation.execute(original.id.value, "other");
      expect(result.type).toBe("FORBIDDEN");
      expect((await repository.findById(original.id))?.isAccepted).toBe(true);
    });

    it("refuses a reservation already canceled", async () => {
      const original = reservationOf(await reserve());
      await services.cancelTableReservation.execute(original.id.value, CUSTOMER);
      const again = await services.cancelTableReservation.execute(original.id.value, CUSTOMER);
      expect(again.type).toBe("NOT_CHANGEABLE");
      expect(publisher.canceled).toHaveLength(1);
    });

    it("refuses a reservation that has already started", async () => {
      const original = reservationOf(await reserve("19:00"));
      clock = at("19:00");
      const result = await services.cancelTableReservation.execute(original.id.value, CUSTOMER);
      expect(result.type).toBe("NOT_CHANGEABLE");
    });

    it("reports an unknown reservation", async () => {
      const result = await services.cancelTableReservation.execute("missing", CUSTOMER);
      expect(result.type).toBe("RESERVATION_NOT_FOUND");
    });
  });

  describe("read a reservation", () => {
    it("returns the reservation by id", async () => {
      const original = reservationOf(await reserve());
      const found = await services.getTableReservation.execute(original.id.value);
      expect(reservationOf(found)).toEqual(original);
    });

    it("returns it to its own customer only", async () => {
      const original = reservationOf(await reserve());
      const own = await services.getTableReservation.execute(original.id.value, CUSTOMER);
      const other = await services.getTableReservation.execute(original.id.value, "other");
      expect(own.type).toBe("SUCCESS");
      expect(other.type).toBe("RESERVATION_NOT_FOUND");
    });

    it("reports an unknown reservation", async () => {
      const result = await services.getTableReservation.execute("missing");
      expect(result.type).toBe("RESERVATION_NOT_FOUND");
    });
  });

  describe("list reservations", () => {
    const starts = async (command: Parameters<ReservationServices["getTableReservations"]["execute"]>[0]) => {
      const result = await services.getTableReservations.execute(command);
      if (result.type !== "SUCCESS") throw new Error(`Expected SUCCESS, got ${result.type}`);
      return result.reservations.map((reservation) => reservation.slot.start);
    };

    beforeEach(async () => {
      await reserve("21:00", 2, CUSTOMER);
      await reserve("19:00", 2, CUSTOMER);
      await reserve("19:00", 2, "customer-2");
    });

    it("lists a customer's reservations, earliest first", async () => {
      expect(await starts({ customerId: CUSTOMER })).toEqual([at("19:00"), at("21:00")]);
    });

    it("lists a restaurant's reservations for its manager", async () => {
      expect(await starts({ restaurantId: RESTAURANT, managerId: MANAGER })).toHaveLength(3);
    });

    it("lists a restaurant's reservations for an internal caller", async () => {
      expect(await starts({ restaurantId: RESTAURANT })).toHaveLength(3);
    });

    it("refuses a manager of another restaurant", async () => {
      const result = await services.getTableReservations.execute({
        restaurantId: RESTAURANT,
        managerId: "another-manager",
      });
      expect(result.type).toBe("FORBIDDEN");
    });

    it("requires a restaurant from a manager", async () => {
      const result = await services.getTableReservations.execute({
        customerId: CUSTOMER,
        managerId: MANAGER,
      });
      expect(result.type).toBe("INVALID_REQUEST");
    });

    it("requires a customer or a restaurant", async () => {
      const result = await services.getTableReservations.execute({});
      expect(result.type).toBe("INVALID_REQUEST");
    });

    it("reports restaurant-service being unavailable", async () => {
      restaurants.available = false;
      const result = await services.getTableReservations.execute({
        restaurantId: RESTAURANT,
        managerId: MANAGER,
      });
      expect(result.type).toBe("RESTAURANT_UNAVAILABLE");
    });
  });
});
