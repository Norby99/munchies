import "./setup-env";
import { describe, it, expect, vi, afterEach } from "vitest";

await import("../../main/ts/infrastructure/adapter/middleware/routes/routes");
const { orderRoutes } = await import("../../main/ts/infrastructure/adapter/middleware/routes/order/order.routes");
const { AdvanceOrderStatusRoute } = await import("../../main/ts/infrastructure/adapter/middleware/routes/order/advance-order-status.route");
const { DiscardOrderRoute } = await import("../../main/ts/infrastructure/adapter/middleware/routes/order/discard-order.route");
const { GetOrderDetailsRoute } = await import("../../main/ts/infrastructure/adapter/middleware/routes/order/get-order-details.route");
const { GetOrdersRoute } = await import("../../main/ts/infrastructure/adapter/middleware/routes/order/get-orders.route");
const { PayOrderRoute } = await import("../../main/ts/infrastructure/adapter/middleware/routes/order/pay-order.route");
const { PlaceOrderRoute } = await import("../../main/ts/infrastructure/adapter/middleware/routes/order/place-order.route");
const { UpdateDeliveryOrderRoute } = await import("../../main/ts/infrastructure/adapter/middleware/routes/order/update-delivery-order.route");
const { UpdateOrderItemsRoute } = await import("../../main/ts/infrastructure/adapter/middleware/routes/order/update-order-items.route");
const { UpdateTakeawayOrderRoute } = await import("../../main/ts/infrastructure/adapter/middleware/routes/order/update-takeaway-order.route");

import { ErrorResponse, AuthRole } from "munchies-commons/kotlin/commons-modules";
import * as internalClient from "../../main/ts/infrastructure/adapter/middleware/routes/internal-client";

function mockResponse() {
  const res: any = {
    statusCode: 200,
    headers: {},
    body: null,
    status: vi.fn(function (code: number) {
      res.statusCode = code;
      return res;
    }),
    type: vi.fn(function () {
      return res;
    }),
    send: vi.fn(function (body: any) {
      res.body = body;
      return res;
    }),
  };
  return res;
}

const dummySuccess = {
  code: 200,
  toJson: () => JSON.stringify({ result: "ok", code: 200 }),
} as any;

describe("Order Routes", () => {
  const originalEnv = process.env.ORDER_SERVICE_URL;

  afterEach(() => {
    if (originalEnv !== undefined) {
      process.env.ORDER_SERVICE_URL = originalEnv;
    } else {
      delete process.env.ORDER_SERVICE_URL;
    }
    vi.restoreAllMocks();
  });

  describe("orderRoutes array", () => {
    it("exports all 9 order routes", () => {
      expect(orderRoutes.length).toBe(9);
    });
  });

  describe("AdvanceOrderStatusRoute", () => {
    it("returns 500 when ORDER_SERVICE_URL is missing", async () => {
      delete process.env.ORDER_SERVICE_URL;
      const route = new AdvanceOrderStatusRoute();
      const res = await route.advanceOrderStatus({ toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles advanceOrderStatus with ORDER_SERVICE_URL", async () => {
      process.env.ORDER_SERVICE_URL = "http://orderservice";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new AdvanceOrderStatusRoute();
      const res = await route.advanceOrderStatus({ toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

    it("handles forward success and catch", async () => {
      process.env.ORDER_SERVICE_URL = "http://orderservice";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new AdvanceOrderStatusRoute();
      vi.spyOn(route as any, "parseRequest").mockReturnValue({ toJson: () => "{}" });

      const resSuccess = await route.forward({ body: "{}" } as any);
      expect(resSuccess).toBe(dummySuccess);

      vi.spyOn(route as any, "parseRequest").mockImplementation(() => {
        throw new Error("parse error");
      });
      const resCatch = await route.forward({ body: "{}" } as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);
    });

    it("handles respond", async () => {
      process.env.ORDER_SERVICE_URL = "http://orderservice";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new AdvanceOrderStatusRoute();
      vi.spyOn(route as any, "parseRequest").mockReturnValue({ toJson: () => "{}" });

      const res = mockResponse();
      await route.respond({ body: "{}" } as any, res);
      expect(res.status).toHaveBeenCalledWith(200);
      expect(res.send).toHaveBeenCalledWith(dummySuccess.toJson());
    });
  });

  describe("DiscardOrderRoute", () => {
    it("returns 500 when ORDER_SERVICE_URL is missing", async () => {
      delete process.env.ORDER_SERVICE_URL;
      const route = new DiscardOrderRoute();
      const res = await route.discardOrder("order-1");
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles discardOrder with ORDER_SERVICE_URL", async () => {
      process.env.ORDER_SERVICE_URL = "http://orderservice";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new DiscardOrderRoute();
      const res = await route.discardOrder("order-1");
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.ORDER_SERVICE_URL = "http://orderservice";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new DiscardOrderRoute();

      const resSuccess = await route.forward({ params: { id: "order-1" } } as any);
      expect(resSuccess).toBe(dummySuccess);

      const resCatch = await route.forward(null as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);

      const res = mockResponse();
      await route.respond({ params: { id: "order-1" } } as any, res);
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  describe("GetOrderDetailsRoute", () => {
    it("returns 500 when ORDER_SERVICE_URL is missing", async () => {
      delete process.env.ORDER_SERVICE_URL;
      const route = new GetOrderDetailsRoute();
      const res = await route.getOrderDetails("order-1");
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles getOrderDetails with ORDER_SERVICE_URL", async () => {
      process.env.ORDER_SERVICE_URL = "http://orderservice";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new GetOrderDetailsRoute();
      const res = await route.getOrderDetails("order-1");
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.ORDER_SERVICE_URL = "http://orderservice";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new GetOrderDetailsRoute();

      const resSuccess = await route.forward({ params: { id: "order-1" } } as any);
      expect(resSuccess).toBe(dummySuccess);

      const resCatch = await route.forward(null as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);

      const res = mockResponse();
      await route.respond({ params: { id: "order-1" } } as any, res);
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  describe("GetOrdersRoute", () => {
    it("returns 500 when ORDER_SERVICE_URL is missing", async () => {
      delete process.env.ORDER_SERVICE_URL;
      const route = new GetOrdersRoute();
      const res = await route.getOrders("rest-1", "cust-1", "PENDING");
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles getOrders with query params and empty query string", async () => {
      process.env.ORDER_SERVICE_URL = "http://orderservice";
      const requestSpy = vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new GetOrdersRoute();

      // With query params
      const res1 = await route.getOrders("rest-1", "cust-1", "PENDING");
      expect(res1).toBe(dummySuccess);
      expect(requestSpy.mock.calls[0][0]).toContain("restaurantId=rest-1");
      expect(requestSpy.mock.calls[0][0]).toContain("customerId=cust-1");
      expect(requestSpy.mock.calls[0][0]).toContain("status=PENDING");

      // Without query params
      const res2 = await route.getOrders(null, null, null);
      expect(res2).toBe(dummySuccess);
      expect(requestSpy.mock.calls[1][0]).not.toContain("?");
    });

    it("handles forward and respond", async () => {
      process.env.ORDER_SERVICE_URL = "http://orderservice";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new GetOrdersRoute();

      const resSuccess = await route.forward({
        query: { restaurantId: "rest-1", customerId: "cust-1", status: "PENDING" },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const resEmptyQuery = await route.forward({ query: {} } as any);
      expect(resEmptyQuery).toBe(dummySuccess);

      const resCatch = await route.forward(null as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);

      const res = mockResponse();
      await route.respond({ query: {} } as any, res);
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  describe("PayOrderRoute", () => {
    it("returns 500 when ORDER_SERVICE_URL is missing", async () => {
      delete process.env.ORDER_SERVICE_URL;
      const route = new PayOrderRoute();
      const res = await route.payOrder("order-1");
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles payOrder with ORDER_SERVICE_URL", async () => {
      process.env.ORDER_SERVICE_URL = "http://orderservice";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new PayOrderRoute();
      const res = await route.payOrder("order-1");
      expect(res).toBe(dummySuccess);
    });

    it("handles forward success and catch", async () => {
      process.env.ORDER_SERVICE_URL = "http://orderservice";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new PayOrderRoute();
      vi.spyOn(route as any, "parseRequest").mockReturnValue("order-1");

      const resSuccess = await route.forward({ body: "{}" } as any);
      expect(resSuccess).toBe(dummySuccess);

      vi.spyOn(route as any, "parseRequest").mockImplementation(() => {
        throw new Error("parse failed");
      });
      const resCatch = await route.forward({ body: "{}" } as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);
    });

    it("handles respond", async () => {
      process.env.ORDER_SERVICE_URL = "http://orderservice";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new PayOrderRoute();
      vi.spyOn(route as any, "parseRequest").mockReturnValue("order-1");

      const res = mockResponse();
      await route.respond({ body: "{}" } as any, res);
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  describe("PlaceOrderRoute", () => {
    it("returns 500 when ORDER_SERVICE_URL is missing", async () => {
      delete process.env.ORDER_SERVICE_URL;
      const route = new PlaceOrderRoute();
      const res = await route.placeOrder({ toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles placeOrder with ORDER_SERVICE_URL", async () => {
      process.env.ORDER_SERVICE_URL = "http://orderservice";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new PlaceOrderRoute();
      const res = await route.placeOrder({ toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.ORDER_SERVICE_URL = "http://orderservice";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new PlaceOrderRoute();
      vi.spyOn(route as any, "parseRequest").mockReturnValue({
        addId: vi.fn().mockReturnValue({ toJson: () => "{}" }),
      });

      const resSuccess = await route.forward({
        body: "{}",
        user: { id: "user-1", role: AuthRole.CUSTOMER },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const resCatch = await route.forward({ body: "{}", user: undefined } as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);

      const res = mockResponse();
      await route.respond({
        body: "{}",
        user: { id: "user-1", role: AuthRole.CUSTOMER },
      } as any, res);
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  describe("UpdateDeliveryOrderRoute", () => {
    it("returns 500 when ORDER_SERVICE_URL is missing", async () => {
      delete process.env.ORDER_SERVICE_URL;
      const route = new UpdateDeliveryOrderRoute();
      const res = await route.updateDeliveryOrderInfo({ toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles updateDeliveryOrderInfo with ORDER_SERVICE_URL", async () => {
      process.env.ORDER_SERVICE_URL = "http://orderservice";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateDeliveryOrderRoute();
      const res = await route.updateDeliveryOrderInfo({ toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.ORDER_SERVICE_URL = "http://orderservice";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateDeliveryOrderRoute();
      vi.spyOn(route as any, "parseRequest").mockReturnValue({
        addId: vi.fn().mockReturnValue({ toJson: () => "{}" }),
      });

      const resSuccess = await route.forward({
        body: "{}",
        user: { id: "user-1", role: AuthRole.CUSTOMER },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const resCatch = await route.forward({ body: "{}", user: undefined } as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);

      const res = mockResponse();
      await route.respond({
        body: "{}",
        user: { id: "user-1", role: AuthRole.CUSTOMER },
      } as any, res);
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  describe("UpdateOrderItemsRoute", () => {
    it("returns 500 when ORDER_SERVICE_URL is missing", async () => {
      delete process.env.ORDER_SERVICE_URL;
      const route = new UpdateOrderItemsRoute();
      const res = await route.updateOrderItems({ toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles updateOrderItems with ORDER_SERVICE_URL", async () => {
      process.env.ORDER_SERVICE_URL = "http://orderservice";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateOrderItemsRoute();
      const res = await route.updateOrderItems({ toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.ORDER_SERVICE_URL = "http://orderservice";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateOrderItemsRoute();
      vi.spyOn(route as any, "parseRequest").mockReturnValue({
        addId: vi.fn().mockReturnValue({ toJson: () => "{}" }),
      });

      const resSuccess = await route.forward({
        body: "{}",
        user: { id: "user-1", role: AuthRole.CUSTOMER },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const resCatch = await route.forward({ body: "{}", user: undefined } as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);

      const res = mockResponse();
      await route.respond({
        body: "{}",
        user: { id: "user-1", role: AuthRole.CUSTOMER },
      } as any, res);
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  describe("UpdateTakeawayOrderRoute", () => {
    it("returns 500 when ORDER_SERVICE_URL is missing", async () => {
      delete process.env.ORDER_SERVICE_URL;
      const route = new UpdateTakeawayOrderRoute();
      const res = await route.updateTakeawayOrderInfo({ toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles updateTakeawayOrderInfo with ORDER_SERVICE_URL", async () => {
      process.env.ORDER_SERVICE_URL = "http://orderservice";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateTakeawayOrderRoute();
      const res = await route.updateTakeawayOrderInfo({ toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.ORDER_SERVICE_URL = "http://orderservice";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateTakeawayOrderRoute();
      vi.spyOn(route as any, "parseRequest").mockReturnValue({
        addId: vi.fn().mockReturnValue({ toJson: () => "{}" }),
      });

      const resSuccess = await route.forward({
        body: "{}",
        user: { id: "user-1", role: AuthRole.CUSTOMER },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const resCatch = await route.forward({ body: "{}", user: undefined } as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);

      const res = mockResponse();
      await route.respond({
        body: "{}",
        user: { id: "user-1", role: AuthRole.CUSTOMER },
      } as any, res);
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });
});
