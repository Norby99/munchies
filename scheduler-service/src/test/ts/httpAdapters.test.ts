import { describe, expect, it } from "vitest";
import type { AxiosInstance } from "axios";
import { HttpOrderClient } from "../../main/ts/infrastructure/outbound/httpOrderClient";
import { HttpRestaurantPolicyClient } from "../../main/ts/infrastructure/outbound/httpRestaurantPolicyClient";
import { HttpRestaurantLocation } from "../../main/ts/infrastructure/outbound/httpRestaurantLocation";
import { OrderServiceError } from "../../main/ts/domain/ports/orderClient";

interface Call {
  method: "get" | "post";
  url: string;
  body?: unknown;
}

/**
 * A stand-in for the axios instance: records every call and answers with a canned status and
 * raw body, the way the adapters ask axios for it (responseType "text").
 */
function fakeHttp(status: number, body: unknown) {
  const calls: Call[] = [];
  const respond = () => Promise.resolve({ status, data: typeof body === "string" ? body : JSON.stringify(body) });
  const http = {
    get: (url: string) => {
      calls.push({ method: "get", url });
      return respond();
    },
    post: (url: string, data: unknown) => {
      calls.push({ method: "post", url, body: data });
      return respond();
    },
  } as unknown as AxiosInstance;
  return { http, calls };
}

const orderDto = {
  orderType: "TAKEAWAY",
  orderId: "order-1",
  restaurantId: "r1",
  customerId: "c1",
  status: "PENDING",
  items: [{ menuItemId: "m1", quantity: 2, unitPriceCents: 450 }],
  payed: false,
  totalCents: 900,
  pickupTime: "1700000000000",
  customerName: "Ada",
};

describe("HttpOrderClient (order-shared contract)", () => {
  it("places an order on the published path with a PlaceOrderRequest body", async () => {
    const { http, calls } = fakeHttp(200, { result: orderDto, code: 200 });
    const placed = await new HttpOrderClient("", http).place({
      restaurantId: "r1",
      customerId: "c1",
      items: [{ menuItemId: "m1", quantity: 2 }],
      orderType: "TAKEAWAY",
      pickupTime: "1700000000000",
      customerName: "Ada",
    });

    expect(placed).toEqual({ orderId: "order-1", status: "PENDING" });
    expect(calls[0].method).toBe("post");
    expect(calls[0].url).toBe("/orders/place");
    const body = JSON.parse(String(calls[0].body));
    expect(body).toMatchObject({
      restaurantId: "r1",
      customerId: "c1",
      orderType: "TAKEAWAY",
      pickupTime: "1700000000000",
      customerName: "Ada",
    });
    expect(body.items).toEqual([{ menuItemId: "m1", quantity: 2, unitPriceCents: 0 }]);
  });

  it("does not leak extra item fields onto the wire", async () => {
    const { http, calls } = fakeHttp(200, { result: orderDto, code: 200 });
    const item = { menuItemId: "m1", quantity: 1, name: "Pizza" } as unknown as { menuItemId: string; quantity: number };
    await new HttpOrderClient("", http).place({
      restaurantId: "r1",
      customerId: "c1",
      items: [item],
      orderType: "TAKEAWAY",
    });
    expect(JSON.parse(String(calls[0].body)).items[0]).not.toHaveProperty("name");
  });

  it("turns an order-service error body into an OrderServiceError with its status", async () => {
    const { http } = fakeHttp(409, { result: "Restaurant closed", code: 409 });
    const error = await new HttpOrderClient("", http)
      .place({ restaurantId: "r1", customerId: "c1", items: [], orderType: "TAKEAWAY" })
      .catch((e: unknown) => e);
    expect(error).toBeInstanceOf(OrderServiceError);
    expect((error as OrderServiceError).status).toBe(409);
    expect((error as OrderServiceError).message).toBe("Restaurant closed");
  });

  it("rejects a response that does not match PlaceOrderResponse as a bad gateway", async () => {
    const { http } = fakeHttp(200, { result: { unexpected: true }, code: 200 });
    const error = await new HttpOrderClient("", http)
      .place({ restaurantId: "r1", customerId: "c1", items: [], orderType: "TAKEAWAY" })
      .catch((e: unknown) => e);
    expect(error).toBeInstanceOf(OrderServiceError);
    expect((error as OrderServiceError).status).toBe(502);
  });

  it("advances an order with an AdvanceOrderStatusRequest body", async () => {
    const { http, calls } = fakeHttp(200, { result: "Order status advanced successfully", code: 200 });
    await new HttpOrderClient("", http).advance("order-1");
    expect(calls[0].url).toBe("/orders/advance");
    expect(JSON.parse(String(calls[0].body))).toEqual({ orderId: "order-1" });
  });

  it("reads the status through GetOrderDetailsResponse", async () => {
    const { http, calls } = fakeHttp(200, { result: { ...orderDto, status: "READY" }, code: 200 });
    expect(await new HttpOrderClient("", http).getStatus("order 1")).toBe("READY");
    expect(calls[0].url).toBe("/orders/order%201");
  });
});

describe("HttpRestaurantPolicyClient (restaurant-shared contract)", () => {
  const policy = {
    slotMinutes: 30,
    maxCapacityUnits: 8,
    opening: "11:00",
    closing: "22:00",
    prepLeadMinutes: 20,
    maxAdvanceDays: 7,
    maxTripsPerSlot: 2,
    maxTripKmBudget: 15.0,
    maxDetourKm: 1.5,
  };

  it("maps GetCapacityPolicyResponse to the domain policy", async () => {
    const { http, calls } = fakeHttp(200, { result: policy, code: 200 });
    expect(await new HttpRestaurantPolicyClient("", http).getPolicy("r1")).toEqual(policy);
    expect(calls[0].url).toBe("/restaurants/r1/capacity-policy/");
  });

  it("returns null when the policy is not configured", async () => {
    const { http } = fakeHttp(404, { result: "Capacity policy not configured", code: 404 });
    expect(await new HttpRestaurantPolicyClient("", http).getPolicy("r1")).toBeNull();
  });

  it("fails on a payload that does not match the contract", async () => {
    const { http } = fakeHttp(200, { result: { slotMinutes: 30 }, code: 200 });
    await expect(new HttpRestaurantPolicyClient("", http).getPolicy("r1")).rejects.toThrow(/capacity policy/);
  });
});

describe("HttpRestaurantLocation (restaurant-shared contract)", () => {
  it("maps GetRestaurantLocationResponse to a GeoPoint", async () => {
    const { http, calls } = fakeHttp(200, { result: { lat: 44.14, lng: 12.24 }, code: 200 });
    expect(await new HttpRestaurantLocation("", http).get("r1")).toEqual({ lat: 44.14, lng: 12.24 });
    expect(calls[0].url).toBe("/restaurants/r1/location/");
  });

  it("returns null when the location is not set or is malformed", async () => {
    expect(await new HttpRestaurantLocation("", fakeHttp(404, "").http).get("r1")).toBeNull();
    expect(await new HttpRestaurantLocation("", fakeHttp(200, { result: {}, code: 200 }).http).get("r1")).toBeNull();
  });
});
