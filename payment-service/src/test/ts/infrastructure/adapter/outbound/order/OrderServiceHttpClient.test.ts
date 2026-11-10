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
});
