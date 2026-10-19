import { afterEach, describe, expect, it } from "vitest";
import express from "express";
import http from "node:http";
import { Writable } from "node:stream";
import pino from "pino";
import {
  createHttpLogger,
  createLogger,
} from "../../main/ts/infrastructure/adapter/middleware/logger";

/** Collects the lines a logger writes, parsed back from JSON. */
function captureLines(): { stream: Writable; lines: () => Record<string, any>[] } {
  const chunks: string[] = [];
  const stream = new Writable({
    write(chunk, _encoding, callback) {
      chunks.push(chunk.toString());
      callback();
    },
  });
  return {
    stream,
    lines: () =>
      chunks
        .join("")
        .split("\n")
        .filter((line) => line.length > 0)
        .map((line) => JSON.parse(line)),
  };
}

/** Same configuration as the application logger, but never silenced by the environment. */
function testLogger(stream: Writable): pino.Logger {
  const log = createLogger(stream);
  log.level = "info";
  return log;
}

async function httpGet(server: http.Server, path: string): Promise<number> {
  const port = (server.address() as { port: number }).port;
  return new Promise((resolve, reject) => {
    http
      .get(
        { hostname: "127.0.0.1", port, path, headers: { cookie: "authToken=secret-token" } },
        (res) => {
          res.resume();
          res.on("end", () => resolve(res.statusCode ?? 0));
        },
      )
      .on("error", reject);
  });
}

describe("logger", () => {
  it("writes one JSON object per line with an upper-case level and the service name", () => {
    const { stream, lines } = captureLines();

    testLogger(stream).warn({ orderId: "42" }, "something happened");

    expect(lines()).toHaveLength(1);
    expect(lines()[0]).toMatchObject({
      level: "WARN",
      service: "gateway-service",
      orderId: "42",
      msg: "something happened",
    });
    expect(lines()[0].time).toMatch(/^\d{4}-\d{2}-\d{2}T/);
  });
});

describe("httpLogger", () => {
  let server: http.Server;

  afterEach(() => {
    if (server?.listening) server.close();
  });

  const startServer = (stream: Writable): Promise<http.Server> =>
    new Promise((resolve) => {
      const app = express();
      app.use(createHttpLogger(testLogger(stream)));
      app.get("/health", (_req, res) => {
        res.status(200).json({ status: "UP" });
      });
      app.get("/orders/:id", (_req, res) => {
        res.status(200).json({ ok: true });
      });
      const s = http.createServer(app);
      s.listen(0, "127.0.0.1", () => resolve(s));
    });

  it("logs a handled request with its method, path and status", async () => {
    const { stream, lines } = captureLines();
    server = await startServer(stream);

    await httpGet(server, "/orders/42");

    expect(lines()).toHaveLength(1);
    expect(lines()[0]).toMatchObject({
      level: "INFO",
      req: { method: "GET", path: "/orders/42" },
      res: { status: 200 },
    });
  });

  it("never writes headers or query strings, which may carry credentials", async () => {
    const { stream, lines } = captureLines();
    server = await startServer(stream);

    await httpGet(server, "/orders/42?token=secret-query");

    const written = JSON.stringify(lines());
    expect(written).not.toContain("secret-token");
    expect(written).not.toContain("secret-query");
  });

  it("does not log operational endpoints", async () => {
    const { stream, lines } = captureLines();
    server = await startServer(stream);

    await httpGet(server, "/health");

    expect(lines()).toHaveLength(0);
  });
});
