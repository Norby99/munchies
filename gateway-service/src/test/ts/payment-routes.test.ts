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
import { processPaymentResponseFromJson } from "munchies-payment-service-shared/kotlin/payment-modules";

const PAYMENT_BODY = JSON.stringify({
  orderId: "order-1",
  paymentDetails: { method: "CARD" },
});

// A real ProcessPaymentResponse: the route tells success from errors by this class.
const paymentResponse = processPaymentResponseFromJson(
  JSON.stringify({ paymentId: "pay-1", status: "COMPLETED", amount: 2350, currency: "EUR" }),
);

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
    const route = new ProcessPaymentRoute();
    const res = await route.forward({ body: Buffer.from(PAYMENT_BODY), user: customer } as any);
    expect(res).toBeInstanceOf(ErrorResponse);
    expect(res.code).toBe(500);
  });

  it("forwards the payment to payment-service, stamped with the caller's id", async () => {
    process.env.PAYMENT_SERVICE_URL = "http://payment-service";
    const spy = vi.spyOn(internalClient, "request").mockResolvedValue(paymentResponse);
    const route = new ProcessPaymentRoute();

    const res = await route.forward({ body: Buffer.from(PAYMENT_BODY), user: customer } as any);

    expect(res).toBeInstanceOf(PaymentEnvelope);
    expect(res.code).toBe(200);
    expect(spy.mock.calls[0][0]).toBe("http://payment-service/payments/");
    expect((spy.mock.calls[0][1] as any).name).toBe("POST");
    expect(JSON.parse(spy.mock.calls[0][2] as string)).toEqual({
      orderId: "order-1",
      customerId: "cust-1",
      paymentDetails: { method: "CARD" },
    });
  });

  it("overrides a customer id sent in the body with the one from the token", async () => {
    process.env.PAYMENT_SERVICE_URL = "http://payment-service";
    const spy = vi.spyOn(internalClient, "request").mockResolvedValue(paymentResponse);
    const body = JSON.stringify({ ...JSON.parse(PAYMENT_BODY), customerId: "someone-else" });

    await new ProcessPaymentRoute().forward({ body: Buffer.from(body), user: customer } as any);

    expect(JSON.parse(spy.mock.calls[0][2] as string).customerId).toBe("cust-1");
  });

  it("wraps the payment-service response in the { result, code } envelope", async () => {
    process.env.PAYMENT_SERVICE_URL = "http://payment-service";
    vi.spyOn(internalClient, "request").mockResolvedValue(paymentResponse);
    const route = new ProcessPaymentRoute();

    const res = mockResponse();
    await route.respond({ body: Buffer.from(PAYMENT_BODY), user: customer } as any, res, () => {});

    expect(res.status).toHaveBeenCalledWith(200);
    expect(JSON.parse(res.body)).toEqual({
      result: { paymentId: "pay-1", status: "COMPLETED", amount: 2350, currency: "EUR" },
      code: 200,
    });
  });

  it("passes payment-service errors through (e.g. order already paid)", async () => {
    process.env.PAYMENT_SERVICE_URL = "http://payment-service";
    vi.spyOn(internalClient, "request").mockResolvedValue(new ErrorResponse("Order already paid", 409));
    const route = new ProcessPaymentRoute();

    const res = await route.forward({ body: Buffer.from(PAYMENT_BODY), user: customer } as any);

    expect(res).toBeInstanceOf(ErrorResponse);
    expect(res.code).toBe(409);
  });

  it("passes errors parsed by payment-shared through, not as a successful envelope", async () => {
    process.env.PAYMENT_SERVICE_URL = "http://payment-service";
    // What the route's own parseError yields for a 403 from payment-service.
    const refusal = new ProcessPaymentRoute().parseError(
      JSON.stringify({ result: "You can only pay your own orders", code: 403 }),
    );
    vi.spyOn(internalClient, "request").mockResolvedValue(refusal);

    const res = mockResponse();
    await new ProcessPaymentRoute().respond(
      { body: Buffer.from(PAYMENT_BODY), user: customer } as any,
      res,
      () => {},
    );

    expect(res.status).toHaveBeenCalledWith(403);
    expect(JSON.parse(res.body)).toEqual({ result: "You can only pay your own orders", code: 403 });
  });

  it("answers 400 to a body that is not a payment request", async () => {
    const route = new ProcessPaymentRoute();
    const res = await route.forward({ body: Buffer.from("{not json"), user: customer } as any);
    expect(res.code).toBe(400);
  });
});
