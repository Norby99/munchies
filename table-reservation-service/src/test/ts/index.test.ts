import { AddressInfo } from "node:net";
import http from "node:http";
import { afterAll, beforeEach, describe, expect, it } from "vitest";
import { createApp, parseBodyToString } from "@main/index";
import { ReservationPolicy } from "@main/domain/model/ReservationPolicy";
import { ReservationBeans } from "@main/infrastructure/adapter/inbound/web/config/ReservationBeans";
import { TableReservationController } from "@main/infrastructure/adapter/inbound/web/controller/controller";
import { InMemoryTableReservationRepository } from "@main/infrastructure/adapter/outbound/memory/InMemoryTableReservationRepository";
import {
  CUSTOMER,
  FakeRestaurantServiceClient,
  MANAGER,
  RESTAURANT,
  RecordingLogger,
  RecordingPublisher,
  now,
} from "@test/support/fakes";

/**
 * The service end to end over HTTP: real routing, controller, use cases and domain, with the
 * in-memory store and with restaurant-service and Kafka replaced by fakes.
 */
describe("table-reservation-service HTTP API", () => {
  let server: http.Server;
  let restaurants: FakeRestaurantServiceClient;
  let publisher: RecordingPublisher;

  async function call(
    method: string,
    path: string,
    body?: unknown,
  ): Promise<{ status: number; json: { result: any; code: number } }> {
    const { port } = server.address() as AddressInfo;
    const response = await fetch(`http://127.0.0.1:${port}${path}`, {
      method,
      headers: { "Content-Type": "application/json" },
      body: body === undefined ? undefined : typeof body === "string" ? body : JSON.stringify(body),
    });
    return { status: response.status, json: await response.json() };
  }

  const reserve = (start = "2026-11-20T19:00:00Z", numberOfGuests = 2, customerId = CUSTOMER) =>
    call("POST", "/reservations/", { restaurantId: RESTAURANT, customerId, start, numberOfGuests });

  beforeEach(async () => {
    restaurants = new FakeRestaurantServiceClient();
    publisher = new RecordingPublisher();
    const controller = new TableReservationController(
      ReservationBeans.createServices({
        repository: new InMemoryTableReservationRepository(),
        restaurantServiceClient: restaurants,
        notificationPublisher: publisher,
        logger: new RecordingLogger(),
        policy: new ReservationPolicy(120),
        now,
      }),
    );
    if (server) await new Promise((resolve) => server.close(resolve));
    server = createApp(controller).listen(0);
  });

  afterAll(async () => {
    await new Promise((resolve) => server.close(resolve));
  });

  it("reports its health", async () => {
    const { port } = server.address() as AddressInfo;
    const response = await fetch(`http://127.0.0.1:${port}/health`);
    expect(response.status).toBe(200);
    expect(await response.json()).toEqual({ status: "UP" });
  });

  it("exposes Prometheus metrics", async () => {
    const { port } = server.address() as AddressInfo;
    const response = await fetch(`http://127.0.0.1:${port}/metrics`);
    expect(response.status).toBe(200);
    expect(await response.text()).toContain("http_server_requests_seconds");
  });

  it("reserves a table and answers 201 with the accepted reservation", async () => {
    const { status, json } = await reserve();

    expect(status).toBe(201);
    expect(json.code).toBe(201);
    expect(json.result).toMatchObject({
      restaurantId: RESTAURANT,
      customerId: CUSTOMER,
      start: "2026-11-20T19:00:00.000Z",
      end: "2026-11-20T21:00:00.000Z",
      numberOfGuests: 2,
      tableNumber: 1,
      status: "ACCEPTED",
    });
    expect(publisher.accepted).toHaveLength(1);
  });

  it("accepts paths with or without the trailing slash", async () => {
    const created = await call("POST", "/reservations", {
      restaurantId: RESTAURANT,
      customerId: CUSTOMER,
      start: "2026-11-20T19:00:00Z",
      numberOfGuests: 2,
    });
    const id = created.json.result.id;

    expect(created.status).toBe(201);
    expect((await call("GET", `/reservations/${id}`)).status).toBe(200);
    expect((await call("GET", `/reservations/${id}/`)).status).toBe(200);
  });

  it("answers 409 when no table is free", async () => {
    await reserve("2026-11-20T19:00:00Z", 4);
    const { status, json } = await reserve("2026-11-20T20:00:00Z", 4, "customer-2");

    expect(status).toBe(409);
    expect(json).toEqual({ result: "No table is free at the requested time", code: 409 });
  });

  it("answers 400 to an invalid reservation", async () => {
    expect((await reserve("2026-11-20T19:05:00Z")).status).toBe(400);
    expect((await reserve("2026-11-20T19:00:00Z", 0)).status).toBe(400);
  });

  it("answers 400 to a body that is not a reservation request", async () => {
    const { status, json } = await call("POST", "/reservations/", "{ not json");
    expect(status).toBe(400);
    expect(json.result).toContain("Malformed request body");
  });

  it("answers 404 for an unknown restaurant", async () => {
    const { status } = await call("POST", "/reservations/", {
      restaurantId: "nowhere",
      customerId: CUSTOMER,
      start: "2026-11-20T19:00:00Z",
      numberOfGuests: 2,
    });
    expect(status).toBe(404);
  });

  it("answers 503 when restaurant-service is unavailable", async () => {
    restaurants.available = false;
    expect((await reserve()).status).toBe(503);
  });

  it("reads a reservation, hiding it from other customers", async () => {
    const id = (await reserve()).json.result.id;

    expect((await call("GET", `/reservations/${id}/?customerId=${CUSTOMER}`)).json.result.id).toBe(
      id,
    );
    expect((await call("GET", `/reservations/${id}/?customerId=someone-else`)).status).toBe(404);
    expect((await call("GET", "/reservations/missing/")).status).toBe(404);
  });

  it("lists a customer's reservations, earliest first", async () => {
    await reserve("2026-11-20T21:00:00Z");
    await reserve("2026-11-20T19:00:00Z");
    await reserve("2026-11-20T19:00:00Z", 2, "customer-2");

    const { status, json } = await call("GET", `/reservations/?customerId=${CUSTOMER}`);

    expect(status).toBe(200);
    expect(json.result.map((reservation: any) => reservation.start)).toEqual([
      "2026-11-20T19:00:00.000Z",
      "2026-11-20T21:00:00.000Z",
    ]);
  });

  it("lists a restaurant's reservations for its manager only", async () => {
    await reserve();
    const path = `/reservations/?restaurantId=${RESTAURANT}&managerId=`;

    expect((await call("GET", path + MANAGER)).json.result).toHaveLength(1);
    expect((await call("GET", path + "another-manager")).status).toBe(403);
    expect((await call("GET", "/reservations/")).status).toBe(400);
  });

  it("modifies a reservation", async () => {
    const id = (await reserve()).json.result.id;

    const { status, json } = await call("PUT", `/reservations/${id}/`, {
      customerId: CUSTOMER,
      start: "2026-11-20T20:30:00Z",
      numberOfGuests: 4,
    });

    expect(status).toBe(200);
    expect(json.result).toMatchObject({
      id,
      start: "2026-11-20T20:30:00.000Z",
      numberOfGuests: 4,
      tableNumber: 2,
    });
    expect(publisher.modified).toHaveLength(1);
  });

  it("refuses to modify or cancel another customer's reservation", async () => {
    const id = (await reserve()).json.result.id;
    const change = { customerId: "someone-else", start: "2026-11-20T20:30:00Z", numberOfGuests: 2 };

    expect((await call("PUT", `/reservations/${id}/`, change)).status).toBe(403);
    expect(
      (await call("PATCH", `/reservations/${id}/cancel/`, { customerId: "someone-else" })).status,
    ).toBe(403);
  });

  it("cancels a reservation once, freeing its table", async () => {
    const id = (await reserve()).json.result.id;
    const cancel = () => call("PATCH", `/reservations/${id}/cancel/`, { customerId: CUSTOMER });

    const first = await cancel();
    expect(first.status).toBe(200);
    expect(first.json.result.status).toBe("CANCELED");
    expect((await cancel()).status).toBe(409);
    expect((await reserve()).json.result.tableNumber).toBe(1);
    expect(publisher.canceled).toHaveLength(1);
  });

  it("answers 500 with the envelope when something unexpected fails", async () => {
    restaurants.getTables = () => Promise.reject(new Error("boom"));
    const { status, json } = await reserve();
    expect(status).toBe(500);
    expect(json).toEqual({ result: "boom", code: 500 });
  });
});

describe("parseBodyToString", () => {
  it("passes strings through and decodes buffers", () => {
    expect(parseBodyToString("hello")).toBe("hello");
    expect(parseBodyToString(Buffer.from("buffered", "utf-8"))).toBe("buffered");
  });

  it("serializes objects and tolerates a missing body", () => {
    expect(parseBodyToString({ key: "value" })).toBe('{"key":"value"}');
    expect(parseBodyToString(undefined)).toBe("");
    expect(parseBodyToString(42)).toBe("42");
  });
});
