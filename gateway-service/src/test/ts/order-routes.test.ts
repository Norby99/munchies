import "./setup-env";
import "../../main/ts/infrastructure/adapter/middleware/routes/routes";
import { describe, it, expect, vi, afterEach } from "vitest";
import { AdvanceOrderStatusRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/order/advance-order-status.route";
import { DiscardOrderRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/order/discard-order.route";
import { GetOrderDetailsRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/order/get-order-details.route";
import { GetOrdersRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/order/get-orders.route";
import { PlaceOrderRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/order/place-order.route";
import { UpdateDeliveryOrderRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/order/update-delivery-order.route";
import { UpdateOrderItemsRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/order/update-order-items.route";
import { UpdateTakeawayOrderRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/order/update-takeaway-order.route";
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

  // ---- AdvanceOrderStatusRoute ----
  describe("AdvanceOrderStatusRoute", () => {
    it("returns 500 when ORDER_SERVICE_URL is missing", async () => {
      delete process.env.ORDER_SERVICE_URL;
      const route = new AdvanceOrderStatusRoute();
      const res = await route.advanceOrderStatus({ toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles advanceOrderStatus with ORDER_SERVICE_URL", async () => {
      process.env.ORDER_SERVICE_URL = "http://order-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new AdvanceOrderStatusRoute();
      const res = await route.advanceOrderStatus({ toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

  });

  // ---- DiscardOrderRoute ----
  describe("DiscardOrderRoute", () => {
    it("returns 500 when ORDER_SERVICE_URL is missing", async () => {
      delete process.env.ORDER_SERVICE_URL;
      const route = new DiscardOrderRoute();
      const res = await route.discardOrder({ toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles discardOrder with ORDER_SERVICE_URL", async () => {
      process.env.ORDER_SERVICE_URL = "http://order-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new DiscardOrderRoute();
      const res = await route.discardOrder({ toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

  });

  // ---- GetOrderDetailsRoute ----
  describe("GetOrderDetailsRoute", () => {
    it("returns 500 when ORDER_SERVICE_URL is missing", async () => {
      delete process.env.ORDER_SERVICE_URL;
      const route = new GetOrderDetailsRoute();
      const res = await route.getOrderDetails("order-1");
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles getOrderDetails with ORDER_SERVICE_URL", async () => {
      process.env.ORDER_SERVICE_URL = "http://order-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new GetOrderDetailsRoute();
      const res = await route.getOrderDetails("order-1");
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.ORDER_SERVICE_URL = "http://order-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new GetOrderDetailsRoute();

      const resSuccess = await route.forward({ params: { id: "order-1" } } as any);
      expect(resSuccess).toBe(dummySuccess);

      const res = mockResponse();
      await route.respond({ params: { id: "order-1" } } as any, res, () => {});
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- GetOrdersRoute ----
  describe("GetOrdersRoute", () => {
    it("returns 500 when ORDER_SERVICE_URL is missing", async () => {
      delete process.env.ORDER_SERVICE_URL;
      const route = new GetOrdersRoute();
      const res = await route.getOrders(null, null, null);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles getOrders with ORDER_SERVICE_URL and no query params", async () => {
      process.env.ORDER_SERVICE_URL = "http://order-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new GetOrdersRoute();
      const res = await route.getOrders(null, null, null);
      expect(res).toBe(dummySuccess);
    });

    it("handles getOrders with all query params", async () => {
      process.env.ORDER_SERVICE_URL = "http://order-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new GetOrdersRoute();
      const res = await route.getOrders("rest-1", "cust-1", "PENDING");
      expect(res).toBe(dummySuccess);
    });

    it("is exposed on the collection root, not on the order-details path", () => {
      const list = new GetOrdersRoute();
      const details = new GetOrderDetailsRoute();
      expect(list.path).toBe("/orders/");
      expect(list.method.name).toBe("GET");
      expect(details.path).toBe("/orders/{id}");
      expect(list.path).not.toBe(details.path);
    });

    it("calls order-service on the collection root, with filters as query parameters", async () => {
      process.env.ORDER_SERVICE_URL = "http://order-service";
      const spy = vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new GetOrdersRoute();

      await route.getOrders(null, null, null);
      expect(spy.mock.calls[0][0]).toBe("http://order-service/orders/");

      await route.getOrders("rest-1", "cust-1", "PENDING");
      expect(spy.mock.calls[1][0]).toBe(
        "http://order-service/orders/?restaurantId=rest-1&customerId=cust-1&status=PENDING",
      );
    });

    it("forces the customer filter to the caller's own id for customers", async () => {
      process.env.ORDER_SERVICE_URL = "http://order-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new GetOrdersRoute();
      const spy = vi.spyOn(route, "getOrders");

      await route.forward({
        query: { restaurantId: "rest-1", customerId: "someone-else", status: "PENDING" },
        user: { id: "cust-1", role: AuthRole.CUSTOMER },
      } as any);
      expect(spy).toHaveBeenCalledWith("rest-1", "cust-1", "PENDING");

      await route.forward({ query: {}, user: { id: "cust-1", role: AuthRole.CUSTOMER } } as any);
      expect(spy).toHaveBeenLastCalledWith(null, "cust-1", null);
    });

    it("keeps the query filters for managers", async () => {
      process.env.ORDER_SERVICE_URL = "http://order-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new GetOrdersRoute();
      const spy = vi.spyOn(route, "getOrders");

      await route.forward({
        query: { restaurantId: "rest-1", customerId: "cust-1", status: "PENDING" },
        user: { id: "manager-1", role: AuthRole.MANAGER },
      } as any);
      expect(spy).toHaveBeenCalledWith("rest-1", "cust-1", "PENDING");

      await route.forward({ query: {}, user: { id: "manager-1", role: AuthRole.MANAGER } } as any);
      expect(spy).toHaveBeenLastCalledWith(null, null, null);
    });

    it("responds with the order-service result", async () => {
      process.env.ORDER_SERVICE_URL = "http://order-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new GetOrdersRoute();

      const res = mockResponse();
      await route.respond(
        { query: {}, user: { id: "cust-1", role: AuthRole.CUSTOMER } } as any,
        res,
        () => {},
      );
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- PlaceOrderRoute ----
  describe("PlaceOrderRoute", () => {
    it("returns 500 when ORDER_SERVICE_URL is missing", async () => {
      delete process.env.ORDER_SERVICE_URL;
      const route = new PlaceOrderRoute();
      const res = await route.placeOrder({ toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles placeOrder with ORDER_SERVICE_URL", async () => {
      process.env.ORDER_SERVICE_URL = "http://order-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new PlaceOrderRoute();
      const res = await route.placeOrder({ toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.ORDER_SERVICE_URL = "http://order-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new PlaceOrderRoute();
      const mockParsedReq = { toJson: () => "{}", addId: vi.fn().mockReturnThis() };
      vi.spyOn(route as any, "parseRequest").mockReturnValue(mockParsedReq);

      const resSuccess = await route.forward({
        body: "{}",
        user: { id: "user-1", role: AuthRole.CUSTOMER },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const resCatch = await route.forward({ body: "{}", user: undefined } as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);

      const res = mockResponse();
      await route.respond({ body: "{}", user: { id: "u1" } } as any, res, () => {});
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- UpdateDeliveryOrderRoute ----
  describe("UpdateDeliveryOrderRoute", () => {
    it("returns 500 when ORDER_SERVICE_URL is missing", async () => {
      delete process.env.ORDER_SERVICE_URL;
      const route = new UpdateDeliveryOrderRoute();
      const res = await route.updateDeliveryOrderInfo({ toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles updateDeliveryOrderInfo with ORDER_SERVICE_URL", async () => {
      process.env.ORDER_SERVICE_URL = "http://order-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateDeliveryOrderRoute();
      const res = await route.updateDeliveryOrderInfo({ toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.ORDER_SERVICE_URL = "http://order-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateDeliveryOrderRoute();
      const mockParsedReq = { toJson: () => "{}", addId: vi.fn().mockReturnThis() };
      vi.spyOn(route as any, "parseRequest").mockReturnValue(mockParsedReq);

      const resSuccess = await route.forward({
        body: "{}",
        user: { id: "user-1", role: AuthRole.CUSTOMER },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const resCatch = await route.forward({ body: "{}", user: undefined } as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);

      const res = mockResponse();
      await route.respond({ body: "{}", user: { id: "u1" } } as any, res, () => {});
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- UpdateOrderItemsRoute ----
  describe("UpdateOrderItemsRoute", () => {
    it("returns 500 when ORDER_SERVICE_URL is missing", async () => {
      delete process.env.ORDER_SERVICE_URL;
      const route = new UpdateOrderItemsRoute();
      const res = await route.updateOrderItems({ toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles updateOrderItems with ORDER_SERVICE_URL", async () => {
      process.env.ORDER_SERVICE_URL = "http://order-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateOrderItemsRoute();
      const res = await route.updateOrderItems({ toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.ORDER_SERVICE_URL = "http://order-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateOrderItemsRoute();
      const mockParsedReq = { toJson: () => "{}", addId: vi.fn().mockReturnThis() };
      vi.spyOn(route as any, "parseRequest").mockReturnValue(mockParsedReq);

      const resSuccess = await route.forward({
        body: "{}",
        user: { id: "user-1", role: AuthRole.CUSTOMER },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const resCatch = await route.forward({ body: "{}", user: undefined } as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);

      const res = mockResponse();
      await route.respond({ body: "{}", user: { id: "u1" } } as any, res, () => {});
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- UpdateTakeawayOrderRoute ----
  describe("UpdateTakeawayOrderRoute", () => {
    it("returns 500 when ORDER_SERVICE_URL is missing", async () => {
      delete process.env.ORDER_SERVICE_URL;
      const route = new UpdateTakeawayOrderRoute();
      const res = await route.updateTakeawayOrderInfo({ toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles updateTakeawayOrderInfo with ORDER_SERVICE_URL", async () => {
      process.env.ORDER_SERVICE_URL = "http://order-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateTakeawayOrderRoute();
      const res = await route.updateTakeawayOrderInfo({ toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.ORDER_SERVICE_URL = "http://order-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateTakeawayOrderRoute();
      const mockParsedReq = { toJson: () => "{}", addId: vi.fn().mockReturnThis() };
      vi.spyOn(route as any, "parseRequest").mockReturnValue(mockParsedReq);

      const resSuccess = await route.forward({
        body: "{}",
        user: { id: "user-1", role: AuthRole.CUSTOMER },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const resCatch = await route.forward({ body: "{}", user: undefined } as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);

      const res = mockResponse();
      await route.respond({ body: "{}", user: { id: "u1" } } as any, res, () => {});
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });
});

