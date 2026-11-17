import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import axios from "axios";
import { OrderServiceHttpClient } from "@main/infrastructure/adapter/outbound/order/OrderServiceHttpClient";

vi.mock("axios");

describe("OrderServiceHttpClient", () => {
  const originalUrl = process.env.ORDER_SERVICE_URL;

  beforeEach(() => {
    process.env.ORDER_SERVICE_URL = "http://order-service:8080";
  });

  afterEach(() => {
    process.env.ORDER_SERVICE_URL = originalUrl;
    vi.restoreAllMocks();
  });

  it("PATCHes the order-service pay endpoint for the given order id", async () => {
    const patchMock = vi
      .spyOn(axios, "patch")
      .mockResolvedValue({ status: 200, data: "" });

    const client = new OrderServiceHttpClient();
    const result = await client.markOrderAsPaid("order-123");

    expect(result).toEqual({ success: true });
    expect(patchMock).toHaveBeenCalledWith(
      "http://order-service:8080/orders/order-123/pay",
      "",
      expect.anything()
    );
  });

  // Regression: without it axios sends application/x-www-form-urlencoded and order-service
  // answers 415, so the order was never marked as paid.
  it("sends the request as JSON", async () => {
    const patchMock = vi
      .spyOn(axios, "patch")
      .mockResolvedValue({ status: 200, data: "" });

    await new OrderServiceHttpClient().markOrderAsPaid("order-123");

    expect(patchMock.mock.calls[0][2]).toMatchObject({
      headers: { "Content-Type": "application/json" },
    });
  });

  it("reports failure when order-service returns an error status", async () => {
    vi.spyOn(axios, "patch").mockResolvedValue({ status: 404, data: "" });

    const client = new OrderServiceHttpClient();
    const result = await client.markOrderAsPaid("missing-order");

    expect(result.success).toBe(false);
    expect(result.errorMessage).toContain("404");
  });

  it("reports failure when the request throws", async () => {
    vi.spyOn(axios, "patch").mockRejectedValue(new Error("network down"));

    const client = new OrderServiceHttpClient();
    const result = await client.markOrderAsPaid("order-123");

    expect(result.success).toBe(false);
    expect(result.errorMessage).toContain("network down");
  });

  it("reports failure when ORDER_SERVICE_URL is not configured", async () => {
    delete process.env.ORDER_SERVICE_URL;

    const client = new OrderServiceHttpClient();
    const result = await client.markOrderAsPaid("order-123");

    expect(result).toEqual({
      success: false,
      errorMessage: "Missing Order Service URL",
    });
  });

  describe("getOrder", () => {
    const orderJson = (overrides: Record<string, unknown> = {}) =>
      JSON.stringify({
        result: {
          orderType: "TAKEAWAY",
          orderId: "order-123",
          restaurantId: "rest-1",
          customerId: "cust-1",
          status: "PENDING",
          items: [{ menuItemId: "item-1", quantity: 2, unitPriceCents: 950 }],
          payed: false,
          totalCents: 1900,
          pickupTime: "1000",
          customerName: "Mario",
          ...overrides,
        },
        code: 200,
      });

    it("GETs the order and returns what is needed to charge it", async () => {
      const getMock = vi
        .spyOn(axios, "get")
        .mockResolvedValue({ status: 200, data: orderJson() });

      const result = await new OrderServiceHttpClient().getOrder("order-123");

      expect(getMock.mock.calls[0][0]).toBe("http://order-service:8080/orders/order-123");
      expect(result).toEqual({
        type: "FOUND",
        order: { customerId: "cust-1", payed: false, totalCents: 1900 },
      });
    });

    it("returns NOT_FOUND on 404", async () => {
      vi.spyOn(axios, "get").mockResolvedValue({ status: 404, data: "" });

      const result = await new OrderServiceHttpClient().getOrder("missing");

      expect(result).toEqual({ type: "NOT_FOUND" });
    });

    it("returns UNAVAILABLE on any other error status", async () => {
      vi.spyOn(axios, "get").mockResolvedValue({ status: 500, data: "" });

      const result = await new OrderServiceHttpClient().getOrder("order-123");

      expect(result.type).toBe("UNAVAILABLE");
    });

    it("returns UNAVAILABLE when the request throws", async () => {
      vi.spyOn(axios, "get").mockRejectedValue(new Error("network down"));

      const result = await new OrderServiceHttpClient().getOrder("order-123");

      expect(result).toEqual({
        type: "UNAVAILABLE",
        reason: "Order Service request failed: network down",
      });
    });

    it("returns UNAVAILABLE when ORDER_SERVICE_URL is not configured", async () => {
      delete process.env.ORDER_SERVICE_URL;

      const result = await new OrderServiceHttpClient().getOrder("order-123");

      expect(result.type).toBe("UNAVAILABLE");
    });
  });
});
