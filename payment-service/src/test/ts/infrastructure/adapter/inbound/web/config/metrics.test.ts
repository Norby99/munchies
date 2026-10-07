import { afterEach, describe, expect, it } from "vitest";
import express from "express";
import http from "node:http";
import {
  UNMATCHED_ROUTE,
  metricsHandler,
  metricsMiddleware,
} from "@main/infrastructure/adapter/inbound/web/config/metrics";

async function httpGet(
  server: http.Server,
  path: string
): Promise<{ status: number; contentType: string; body: string }> {
  const port = (server.address() as { port: number }).port;
  return new Promise((resolve, reject) => {
    http
      .get({ hostname: "127.0.0.1", port, path }, (res) => {
        let data = "";
        res.on("data", (chunk) => (data += chunk));
        res.on("end", () =>
          resolve({
            status: res.statusCode ?? 0,
            contentType: String(res.headers["content-type"]),
            body: data,
          })
        );
      })
      .on("error", reject);
  });
}

describe("metrics", () => {
  let server: http.Server;

  afterEach(() => {
    if (server?.listening) server.close();
  });

  const startServer = (): Promise<http.Server> =>
    new Promise((resolve) => {
      const app = express();
      app.use(metricsMiddleware());
      app.get("/orders/:id", (_req, res) => {
        res.status(200).json({ ok: true });
      });
      app.get("/metrics", metricsHandler);
      const s = http.createServer(app);
      s.listen(0, "127.0.0.1", () => resolve(s));
    });

  it("exposes the registry in the Prometheus text format", async () => {
    server = await startServer();

    const res = await httpGet(server, "/metrics");

    expect(res.status).toBe(200);
    expect(res.contentType).toContain("text/plain");
    expect(res.body).toContain("# TYPE http_server_requests_seconds histogram");
    expect(res.body).toContain("nodejs_eventloop_lag_seconds");
  });

  it("records a handled request under its route template, not its concrete path", async () => {
    server = await startServer();

    await httpGet(server, "/orders/42");
    const res = await httpGet(server, "/metrics");

    expect(res.body).toContain(
      'http_server_requests_seconds_count{method="GET",uri="/orders/:id",status="200"} 1'
    );
    expect(res.body).not.toContain("/orders/42");
  });

  it("groups requests that match no route under a single label value", async () => {
    server = await startServer();

    await httpGet(server, "/does-not-exist/1");
    await httpGet(server, "/does-not-exist/2");
    const res = await httpGet(server, "/metrics");

    expect(res.body).toContain(
      `http_server_requests_seconds_count{method="GET",uri="${UNMATCHED_ROUTE}",status="404"} 2`
    );
  });
});
