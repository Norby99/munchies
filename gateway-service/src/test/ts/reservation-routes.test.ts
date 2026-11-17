import "./setup-env";
import "../../main/ts/infrastructure/adapter/middleware/routes/routes";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { AuthRole, ErrorResponse } from "munchies-commons/kotlin/commons-modules";
import {
  tableReservationResponseFromJson,
  tableReservationsResponseFromJson,
} from "munchies-table-reservation-service-shared/kotlin/table-reservation-modules";
import { applyRoutes } from "../../main/ts/infrastructure/adapter/middleware/routes/routes";
import * as internalClient from "../../main/ts/infrastructure/adapter/middleware/routes/internal-client";
import { CancelTableReservationRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/reservation/cancel-table-reservation.route";
import { GetTableReservationRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/reservation/get-table-reservation.route";
import { GetTableReservationsRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/reservation/get-table-reservations.route";
import { ModifyTableReservationRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/reservation/modify-table-reservation.route";
import { ReserveTableRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/reservation/reserve-table.route";
import { GetTablesRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/get-tables.route";
import { UpdateTablesRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/update-tables.route";

const RESERVATION = {
  id: "res-1",
  restaurantId: "rest-1",
  customerId: "cust-1",
  start: "2026-11-20T19:00:00.000Z",
  end: "2026-11-20T21:00:00.000Z",
  numberOfGuests: 2,
  tableNumber: 1,
  status: "ACCEPTED",
};
const reservationResponse = tableReservationResponseFromJson(
  JSON.stringify({ result: RESERVATION, code: 200 }),
);
const reservationsResponse = tableReservationsResponseFromJson(
  JSON.stringify({ result: [RESERVATION], code: 200 }),
);

const customer = { id: "cust-1", role: AuthRole.CUSTOMER };
const manager = { id: "mgr-1", role: AuthRole.MANAGER };

function mockResponse() {
  const res: any = {
    status: vi.fn(() => res),
    type: vi.fn(() => res),
    send: vi.fn((body: string) => {
      res.body = body;
      return res;
    }),
  };
  return res;
}

describe("table reservation routes", () => {
  const originalUrl = process.env.TABLE_RESERVATION_SERVICE_URL;

  beforeEach(() => {
    process.env.TABLE_RESERVATION_SERVICE_URL = "http://reservation-service";
  });

  afterEach(() => {
    if (originalUrl === undefined) delete process.env.TABLE_RESERVATION_SERVICE_URL;
    else process.env.TABLE_RESERVATION_SERVICE_URL = originalUrl;
    vi.restoreAllMocks();
  });

  it("are registered on the gateway for authenticated users", () => {
    const registered: string[] = [];
    const record = (method: string) =>
      vi.fn((path: string) => {
        registered.push(`${method} ${path}`);
        return app;
      });
    const app: any = {
      get: record("GET"),
      post: record("POST"),
      put: record("PUT"),
      patch: record("PATCH"),
      delete: record("DELETE"),
    };

    applyRoutes(app);

    expect(registered).toEqual(
      expect.arrayContaining([
        "POST /reservations/",
        "GET /reservations/",
        "GET /reservations/:id/",
        "PUT /reservations/:id/",
        "PATCH /reservations/:id/cancel/",
        "GET /restaurants/:restaurantId/tables/",
        "PUT /restaurants/:restaurantId/tables/",
      ]),
    );
    for (const route of [
      new ReserveTableRoute(),
      new GetTableReservationRoute(),
      new GetTableReservationsRoute(),
      new ModifyTableReservationRoute(),
      new CancelTableReservationRoute(),
    ]) {
      expect(route.authRole?.name).toBe(AuthRole.CUSTOMER.name);
    }
  });

  it("answer 500 when TABLE_RESERVATION_SERVICE_URL is missing", async () => {
    delete process.env.TABLE_RESERVATION_SERVICE_URL;
    const spy = vi.spyOn(internalClient, "request");

    const res = await new GetTableReservationRoute().forward({
      params: { id: "res-1" },
      user: customer,
    } as any);

    expect(res.code).toBe(500);
    expect(spy).not.toHaveBeenCalled();
  });

  describe("reserve a table", () => {
    const body = (extra: object = {}) =>
      Buffer.from(
        JSON.stringify({
          restaurantId: "rest-1",
          start: "2026-11-20T19:00:00Z",
          numberOfGuests: 2,
          ...extra,
        }),
      );

    it("forwards the request stamped with the caller's id, ignoring the one in the body", async () => {
      const spy = vi.spyOn(internalClient, "request").mockResolvedValue(reservationResponse);

      const res = await new ReserveTableRoute().forward({
        body: body({ customerId: "someone-else" }),
        user: customer,
      } as any);

      expect(res).toBe(reservationResponse);
      expect(spy.mock.calls[0][0]).toBe("http://reservation-service/reservations/");
      expect((spy.mock.calls[0][1] as any).name).toBe("POST");
      expect(JSON.parse(spy.mock.calls[0][2] as string)).toEqual({
        restaurantId: "rest-1",
        customerId: "cust-1",
        start: "2026-11-20T19:00:00Z",
        numberOfGuests: 2,
      });
    });

    it("answers with the reservation envelope and the upstream status", async () => {
      const created = tableReservationResponseFromJson(
        JSON.stringify({ result: RESERVATION, code: 201 }),
      );
      vi.spyOn(internalClient, "request").mockResolvedValue(created);

      const res = mockResponse();
      await new ReserveTableRoute().respond({ body: body(), user: customer } as any, res, () => {});

      expect(res.status).toHaveBeenCalledWith(201);
      expect(JSON.parse(res.body)).toEqual({ result: RESERVATION, code: 201 });
    });

    it("passes upstream refusals through (e.g. no table available)", async () => {
      vi.spyOn(internalClient, "request").mockResolvedValue(
        new ErrorResponse("No table is free at the requested time", 409),
      );

      const res = await new ReserveTableRoute().forward({ body: body(), user: customer } as any);

      expect(res.code).toBe(409);
    });

    it("answers 400 to a body that is not a reservation request", async () => {
      const res = await new ReserveTableRoute().forward({
        body: Buffer.from("{not json"),
        user: customer,
      } as any);
      expect(res.code).toBe(400);
    });
  });

  describe("read a reservation", () => {
    it("restricts a customer to their own reservation", async () => {
      const spy = vi.spyOn(internalClient, "request").mockResolvedValue(reservationResponse);

      await new GetTableReservationRoute().forward({
        params: { id: "res-1" },
        user: customer,
      } as any);

      expect(spy.mock.calls[0][0]).toBe(
        "http://reservation-service/reservations/res-1/?customerId=cust-1",
      );
    });

    it("applies no owner filter for a manager", async () => {
      const spy = vi.spyOn(internalClient, "request").mockResolvedValue(reservationResponse);

      await new GetTableReservationRoute().forward({
        params: { id: "res-1" },
        user: manager,
      } as any);

      expect(spy.mock.calls[0][0]).toBe("http://reservation-service/reservations/res-1/");
    });
  });

  describe("list reservations", () => {
    it("lists a customer's own reservations, whatever the query string says", async () => {
      const spy = vi.spyOn(internalClient, "request").mockResolvedValue(reservationsResponse);

      const res = await new GetTableReservationsRoute().forward({
        query: { customerId: "someone-else", managerId: "mgr-1" },
        user: customer,
      } as any);

      expect(res).toBe(reservationsResponse);
      expect(spy.mock.calls[0][0]).toBe(
        "http://reservation-service/reservations/?customerId=cust-1",
      );
    });

    it("lists a restaurant's reservations on behalf of the manager in the token", async () => {
      const spy = vi.spyOn(internalClient, "request").mockResolvedValue(reservationsResponse);

      await new GetTableReservationsRoute().forward({
        query: { restaurantId: "rest-1", managerId: "another-manager" },
        user: manager,
      } as any);

      expect(spy.mock.calls[0][0]).toBe(
        "http://reservation-service/reservations/?restaurantId=rest-1&managerId=mgr-1",
      );
    });
  });

  describe("modify a reservation", () => {
    it("forwards the change stamped with the caller's id", async () => {
      const spy = vi.spyOn(internalClient, "request").mockResolvedValue(reservationResponse);

      await new ModifyTableReservationRoute().forward({
        params: { id: "res-1" },
        body: Buffer.from(
          JSON.stringify({ start: "2026-11-20T20:00:00Z", numberOfGuests: 4 }),
        ),
        user: customer,
      } as any);

      expect(spy.mock.calls[0][0]).toBe("http://reservation-service/reservations/res-1/");
      expect((spy.mock.calls[0][1] as any).name).toBe("PUT");
      expect(JSON.parse(spy.mock.calls[0][2] as string)).toEqual({
        customerId: "cust-1",
        start: "2026-11-20T20:00:00Z",
        numberOfGuests: 4,
      });
    });

    it("answers 400 to a body that is not a change request", async () => {
      const res = await new ModifyTableReservationRoute().forward({
        params: { id: "res-1" },
        body: Buffer.from("{}"),
        user: customer,
      } as any);
      expect(res.code).toBe(400);
    });
  });

  describe("cancel a reservation", () => {
    it("forwards the cancellation on behalf of the caller, with no client body needed", async () => {
      const spy = vi.spyOn(internalClient, "request").mockResolvedValue(reservationResponse);

      await new CancelTableReservationRoute().forward({
        params: { id: "res-1" },
        user: customer,
      } as any);

      expect(spy.mock.calls[0][0]).toBe("http://reservation-service/reservations/res-1/cancel/");
      expect((spy.mock.calls[0][1] as any).name).toBe("PATCH");
      expect(JSON.parse(spy.mock.calls[0][2] as string)).toEqual({ customerId: "cust-1" });
    });
  });
});

describe("restaurant table routes", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("let any authenticated user read the tables, and only managers change them", () => {
    expect(new GetTablesRoute().authRole?.name).toBe(AuthRole.CUSTOMER.name);
    expect(new UpdateTablesRoute().authRole?.name).toBe(AuthRole.MANAGER.name);
  });

  it("reads the tables of a restaurant", async () => {
    const spy = vi.spyOn(internalClient, "request").mockResolvedValue(new ErrorResponse("", 200));

    await new GetTablesRoute().forward({ params: { restaurantId: "rest-1" } } as any);

    expect(spy.mock.calls[0][0]).toBe("http://restaurant-service/restaurants/rest-1/tables/");
    expect((spy.mock.calls[0][1] as any).name).toBe("GET");
  });

  it("replaces the tables on behalf of the manager in the token", async () => {
    const spy = vi.spyOn(internalClient, "request").mockResolvedValue(new ErrorResponse("", 200));

    await new UpdateTablesRoute().forward({
      params: { restaurantId: "rest-1" },
      body: Buffer.from(
        JSON.stringify({ managerId: "someone-else", tables: [{ number: 1, seats: 2 }] }),
      ),
      user: manager,
    } as any);

    expect(spy.mock.calls[0][0]).toBe("http://restaurant-service/restaurants/rest-1/tables/");
    expect((spy.mock.calls[0][1] as any).name).toBe("PUT");
    expect(JSON.parse(spy.mock.calls[0][2] as string)).toEqual({
      managerId: "mgr-1",
      tables: [{ number: 1, seats: 2 }],
    });
  });
});
