import { afterEach, describe, expect, it, vi } from "vitest";
import http from "node:http";
import { createManagementApp } from "@main/infrastructure/adapter/inbound/web/config/managementApp";
import { NotificationController } from "@main/infrastructure/adapter/inbound/web/controller/controller";
import { UserEmailConfirmationNotification } from "@main/domain/external-modules";

async function httpGet(
  server: http.Server,
  path: string
): Promise<{ status: number; body: string }> {
  const port = (server.address() as { port: number }).port;
  return new Promise((resolve, reject) => {
    http
      .get({ hostname: "127.0.0.1", port, path }, (res) => {
        let data = "";
        res.on("data", (chunk) => (data += chunk));
        res.on("end", () => resolve({ status: res.statusCode ?? 0, body: data }));
      })
      .on("error", reject);
  });
}

describe("createManagementApp", () => {
  let server: http.Server;

  afterEach(() => {
    if (server?.listening) server.close();
    vi.restoreAllMocks();
  });

  const startServer = (): Promise<http.Server> =>
    new Promise((resolve) => {
      const s = http.createServer(createManagementApp());
      s.listen(0, "127.0.0.1", () => resolve(s));
    });

  it("responds 200 with { status: 'UP' } on GET /health", async () => {
    server = await startServer();

    const res = await httpGet(server, "/health");

    expect(res.status).toBe(200);
    expect(JSON.parse(res.body)).toEqual({ status: "UP" });
  });

  it("exposes Node.js runtime metrics on GET /metrics", async () => {
    server = await startServer();

    const res = await httpGet(server, "/metrics");

    expect(res.status).toBe(200);
    expect(res.body).toContain("nodejs_eventloop_lag_seconds");
  });

  it("counts every handled notification by type", async () => {
    vi.spyOn(console, "log").mockImplementation(() => {});
    server = await startServer();

    new NotificationController().handleUserEmailConfirmation(
      new UserEmailConfirmationNotification("user-1", "otk-123")
    );
    const res = await httpGet(server, "/metrics");

    expect(res.body).toMatch(
      /notifications_received_total\{type="user_email_confirmation"\} [1-9]/
    );
  });
});
