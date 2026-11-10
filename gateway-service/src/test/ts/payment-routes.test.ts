import "./setup-env";
import "../../main/ts/infrastructure/adapter/middleware/routes/routes";
import { afterEach, describe, expect, it, vi } from "vitest";
import { AuthRole, ErrorResponse } from "munchies-commons/kotlin/commons-modules";
import { applyRoutes } from "../../main/ts/infrastructure/adapter/middleware/routes/routes";
import * as internalClient from "../../main/ts/infrastructure/adapter/middleware/routes/internal-client";
import {
  PaymentEnvelope,
  ProcessPaymentRoute,
} from "../../main/ts/infrastructure/adapter/middleware/routes/payment/process-payment.route";

const PAYMENT_BODY = JSON.stringify({
  orderId: "order-1",
  paymentDetails: { amount: 2350, method: "CARD", currency: "EUR" },
});

// Stand-in for the order-details lookup the route performs before charging.
function ordersReturning(result: { customerId: string; payed: boolean } | ErrorResponse) {
  return {
    getOrderDetails: vi.fn().mockResolvedValue(
      result instanceof ErrorResponse ? result : { code: 200, result },
    ),
  } as any;
}

const paymentResponse = {
  paymentId: "pay-1",
  toJson: () => JSON.stringify({ paymentId: "pay-1", status: "COMPLETED", amount: 2350, currency: "EUR" }),
} as any;

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

const customer = { id: "cust-1", role: AuthRole.CUSTOMER };

describe("ProcessPaymentRoute", () => {
  const originalUrl = process.env.PAYMENT_SERVICE_URL;

  afterEach(() => {
    if (originalUrl === undefined) delete process.env.PAYMENT_SERVICE_URL;
    else process.env.PAYMENT_SERVICE_URL = originalUrl;
    vi.restoreAllMocks();
  });

  it("is exposed as POST /payments/ for customers", () => {
    const route = new ProcessPaymentRoute();
    expect(route.path).toBe("/payments/");
    expect(route.method.name).toBe("POST");
    expect(route.authRole?.name).toBe(AuthRole.CUSTOMER.name);
  });

  it("is registered on the gateway, while PATCH /orders/{id}/pay is not", () => {
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

    expect(registered).toContain("POST /payments/");
    expect(registered.some((r) => r.startsWith("PATCH /orders/") && r.endsWith("/pay"))).toBe(false);
  });

  it("returns 500 when PAYMENT_SERVICE_URL is missing", async () => {
    delete process.env.PAYMENT_SERVICE_URL;
    const route = new ProcessPaymentRoute(ordersReturning({ customerId: "cust-1", payed: false }));
    const res = await route.forward({ body: Buffer.from(PAYMENT_BODY), user: customer } as any);
    expect(res).toBeInstanceOf(ErrorResponse);
    expect(res.code).toBe(500);
  });

  it("forwards the payment of the caller's unpaid order to payment-service", async () => {
    process.env.PAYMENT_SERVICE_URL = "http://payment-service";
    const spy = vi.spyOn(internalClient, "request").mockResolvedValue(paymentResponse);
    const orders = ordersReturning({ customerId: "cust-1", payed: false });
    const route = new ProcessPaymentRoute(orders);

    const res = await route.forward({ body: Buffer.from(PAYMENT_BODY), user: customer } as any);

    expect(orders.getOrderDetails).toHaveBeenCalledWith("order-1");
    expect(res).toBeInstanceOf(PaymentEnvelope);
    expect(res.code).toBe(200);
    expect(spy.mock.calls[0][0]).toBe("http://payment-service/payments/");
    expect((spy.mock.calls[0][1] as any).name).toBe("POST");
    expect(JSON.parse(spy.mock.calls[0][2] as string)).toMatchObject({
      orderId: "order-1",
      paymentDetails: { amount: 2350, method: "CARD", currency: "EUR" },
    });
  });

  it("wraps the payment-service response in the { result, code } envelope", async () => {
    process.env.PAYMENT_SERVICE_URL = "http://payment-service";
    vi.spyOn(internalClient, "request").mockResolvedValue(paymentResponse);
    const route = new ProcessPaymentRoute(ordersReturning({ customerId: "cust-1", payed: false }));

    const res = mockResponse();
    await route.respond({ body: Buffer.from(PAYMENT_BODY), user: customer } as any, res, () => {});

    expect(res.status).toHaveBeenCalledWith(200);
    expect(JSON.parse(res.body)).toEqual({
      result: { paymentId: "pay-1", status: "COMPLETED", amount: 2350, currency: "EUR" },
      code: 200,
    });
  });

  it("refuses to pay another customer's order, without charging", async () => {
    process.env.PAYMENT_SERVICE_URL = "http://payment-service";
    const spy = vi.spyOn(internalClient, "request");
    const route = new ProcessPaymentRoute(ordersReturning({ customerId: "someone-else", payed: false }));

    const res = await route.forward({ body: Buffer.from(PAYMENT_BODY), user: customer } as any);

    expect(res.code).toBe(403);
    expect(spy).not.toHaveBeenCalled();
  });

  it("refuses to pay an order twice, without charging", async () => {
    process.env.PAYMENT_SERVICE_URL = "http://payment-service";
    const spy = vi.spyOn(internalClient, "request");
    const route = new ProcessPaymentRoute(ordersReturning({ customerId: "cust-1", payed: true }));

    const res = await route.forward({ body: Buffer.from(PAYMENT_BODY), user: customer } as any);

    expect(res.code).toBe(409);
    expect(spy).not.toHaveBeenCalled();
  });

  it("propagates the order lookup error (e.g. unknown order), without charging", async () => {
    process.env.PAYMENT_SERVICE_URL = "http://payment-service";
    const spy = vi.spyOn(internalClient, "request");
    const route = new ProcessPaymentRoute(ordersReturning(new ErrorResponse("Order not found", 404)));

    const res = await route.forward({ body: Buffer.from(PAYMENT_BODY), user: customer } as any);

    expect(res.code).toBe(404);
    expect(spy).not.toHaveBeenCalled();
  });

  it("passes payment-service errors through (e.g. payment rejected)", async () => {
    process.env.PAYMENT_SERVICE_URL = "http://payment-service";
    vi.spyOn(internalClient, "request").mockResolvedValue(new ErrorResponse("Payment rejected", 400));
    const route = new ProcessPaymentRoute(ordersReturning({ customerId: "cust-1", payed: false }));

    const res = await route.forward({ body: Buffer.from(PAYMENT_BODY), user: customer } as any);

    expect(res).toBeInstanceOf(ErrorResponse);
    expect(res.code).toBe(400);
  });

  it("answers 400 to a body that is not a payment request", async () => {
    const route = new ProcessPaymentRoute(ordersReturning({ customerId: "cust-1", payed: false }));
    const res = await route.forward({ body: Buffer.from("{not json"), user: customer } as any);
    expect(res.code).toBe(400);
  });
});
