import { describe, expect, it, vi, beforeEach, afterEach } from "vitest";
import http from "node:http";
import { createApp, parseBodyToString } from "@main/index";
import { PaymentController } from "@main/infrastructure/adapter/inbound/web/controller/controller";
import {
  Currency,
  PaymentMethod,
  PaymentStatus,
  ProcessPaymentResponse,
} from "munchies-payment-service-shared/kotlin/payment-modules";

// ── Tiny HTTP helper ──────────────────────────────────────────────────────────

async function httpPost(
  server: http.Server,
  path: string,
  body: string
): Promise<{ status: number; body: string }> {
  const port = (server.address() as { port: number }).port;
  return new Promise((resolve, reject) => {
    const req = http.request(
      { hostname: "127.0.0.1", port, path, method: "POST" },
      (res) => {
        let data = "";
        res.on("data", (chunk) => (data += chunk));
        res.on("end", () => resolve({ status: res.statusCode ?? 0, body: data }));
      }
    );
    req.on("error", reject);
    req.setHeader("Content-Type", "application/json");
    req.write(body);
    req.end();
  });
}

async function httpGet(
  server: http.Server,
  path: string
): Promise<{ status: number; body: string }> {
  const port = (server.address() as { port: number }).port;
  return new Promise((resolve, reject) => {
    http.get({ hostname: "127.0.0.1", port, path }, (res) => {
      let data = "";
      res.on("data", (chunk) => (data += chunk));
      res.on("end", () => resolve({ status: res.statusCode ?? 0, body: data }));
    }).on("error", reject);
  });
}

// ── parseBodyToString ────────────────────────────────────────────────────────

describe("parseBodyToString", () => {
  it("returns the string unchanged", () => {
    expect(parseBodyToString("hello")).toBe("hello");
  });

  it("converts a Buffer to a UTF-8 string", () => {
    const buf = Buffer.from("buffered", "utf-8");
    expect(parseBodyToString(buf)).toBe("buffered");
  });

  it("JSON-stringifies a plain object", () => {
    expect(parseBodyToString({ key: "value" })).toBe('{"key":"value"}');
  });

  it("converts a number to its string representation", () => {
    expect(parseBodyToString(42)).toBe("42");
  });

  it("returns an empty string for null", () => {
    expect(parseBodyToString(null)).toBe("");
  });

  it("returns an empty string for undefined", () => {
    expect(parseBodyToString(undefined)).toBe("");
  });
});

// ── createApp ────────────────────────────────────────────────────────────────

describe("createApp", () => {
  let server: http.Server;

  afterEach(() => {
    if (server?.listening) server.close();
  });

  const startServer = (controller?: PaymentController): Promise<http.Server> =>
    new Promise((resolve) => {
      const app = createApp(controller);
      const s = http.createServer(app);
      s.listen(0, "127.0.0.1", () => resolve(s));
    });

  describe("GET /health", () => {
    it("responds 200 with { status: 'UP' }", async () => {
      server = await startServer();
      const res = await httpGet(server, "/health");

      expect(res.status).toBe(200);
      expect(JSON.parse(res.body)).toEqual({ status: "UP" });
    });
  });

  describe("POST /payments", () => {
    let mockController: PaymentController;

    beforeEach(() => {
      mockController = {
        processPayment: vi.fn(),
      } as unknown as PaymentController;
    });

    it("returns 400 with an error payload when the controller throws an Error", async () => {
      vi.mocked(mockController.processPayment).mockRejectedValue(
        new Error("Payment rejected")
      );
      server = await startServer(mockController);

      const body = JSON.stringify({
        orderId: "order-bad",
        paymentDetails: { amount: -1, method: "CARD", currency: "EUR" },
      });
      const res = await httpPost(server, "/payments", body);

      expect(res.status).toBe(400);
      expect(res.body).toContain("Payment rejected");
    });

    it("returns 400 with stringified error when a non-Error is thrown", async () => {
      vi.mocked(mockController.processPayment).mockRejectedValue("string error");
      server = await startServer(mockController);

      const body = JSON.stringify({ orderId: "x", paymentDetails: {} });
      const res = await httpPost(server, "/payments", body);

      expect(res.status).toBe(400);
    });
  });
});
