import { Request, RequestHandler, Response } from "express";
import { Histogram, Registry, collectDefaultMetrics } from "prom-client";

export const metricsRegistry = new Registry();

collectDefaultMetrics({ register: metricsRegistry });

const httpRequestDuration = new Histogram({
  name: "http_server_requests_seconds",
  help: "Duration of handled HTTP requests, in seconds",
  labelNames: ["method", "uri", "status"],
  buckets: [0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1, 2.5, 5, 10],
  registers: [metricsRegistry],
});

export const UNMATCHED_ROUTE = "UNMATCHED";

function routeTemplate(req: Request): string {
  return req.route ? req.baseUrl + req.route.path : UNMATCHED_ROUTE;
}

export function metricsMiddleware(): RequestHandler {
  return (req, res, next) => {
    const stopTimer = httpRequestDuration.startTimer();
    res.on("finish", () => {
      stopTimer({
        method: req.method,
        uri: routeTemplate(req),
        status: String(res.statusCode),
      });
    });
    next();
  };
}

export async function metricsHandler(_req: Request, res: Response): Promise<void> {
  res
    .status(200)
    .set("Content-Type", metricsRegistry.contentType)
    .send(await metricsRegistry.metrics());
}
