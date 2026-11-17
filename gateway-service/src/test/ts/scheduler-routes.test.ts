import "./setup-env";
import http from "node:http";
import { AddressInfo } from "node:net";
import { afterAll, beforeAll, describe, expect, it } from "vitest";
import { AuthRole } from "munchies-commons/kotlin/commons-modules";
import { AuthedRequest } from "../../main/ts/infrastructure/adapter/middleware/auth";
import { schedulerRoutes } from "../../main/ts/infrastructure/adapter/middleware/routes/scheduler/scheduler.routes";

const TRACKING = "/schedule/orders/{orderId}/tracking";

// A stand-in scheduler that echoes what the gateway sent it.
let scheduler: http.Server;
let received: { url?: string; headers: http.IncomingHttpHeaders } = { headers: {} };

beforeAll(async () => {
  scheduler = http.createServer((req, res) => {
    received = { url: req.url, headers: req.headers };
    res.writeHead(200, { "Content-Type": "application/json" }).end('{"result":"ok","code":200}');
  });
  await new Promise<void>((resolve) => scheduler.listen(0, "127.0.0.1", resolve));
  process.env.SCHEDULER_SERVICE_URL = `http://127.0.0.1:${(scheduler.address() as AddressInfo).port}`;
});

afterAll(async () => {
  await new Promise((resolve) => scheduler.close(resolve));
});

describe("Scheduler tracking route", () => {
  const route = schedulerRoutes.find((r) => r.path === TRACKING);

  it("is readable by customers (and so by managers)", () => {
    expect(route?.method.name).toBe("GET");
    expect(route?.authRole?.name).toBe(AuthRole.CUSTOMER.name);
  });

  it("forwards the verified identity, not the one the client claims", async () => {
    const req = {
      params: { orderId: "o1" },
      originalUrl: "/schedule/orders/o1/tracking",
      headers: { "x-user-id": "someone-else", "x-user-role": "MANAGER" },
      user: { id: "customer-1", role: AuthRole.CUSTOMER },
    } as unknown as AuthedRequest;

    const result = await route!.forward(req);

    expect(result.code).toBe(200);
    expect(received.url).toBe("/schedule/orders/o1/tracking");
    expect(received.headers["x-user-id"]).toBe("customer-1");
    expect(received.headers["x-user-role"]).toBe(AuthRole.CUSTOMER.name);
  });
});
