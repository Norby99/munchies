import { IncomingMessage, ServerResponse } from "node:http";
import pino from "pino";
import pinoHttp from "pino-http";

/**
 * Application logger (Log Aggregation side of the Observability pattern).
 *
 * Every line is one JSON object on stdout. The service does not know where its
 * logs end up: in the cluster they are tailed by Alloy and stored in Loki (see
 * `observability/`).
 *
 * The severity is written as an upper-case name (`INFO`, `ERROR`), the same
 * values Logback emits for the Kotlin services, so one filter covers both stacks.
 *
 * @param destination where lines are written; stdout when omitted.
 */
export function createLogger(destination?: pino.DestinationStream): pino.Logger {
  return pino(
    {
      level: process.env.LOG_LEVEL ?? "info",
      base: { service: "gateway-service" },
      timestamp: pino.stdTimeFunctions.isoTime,
      formatters: {
        level: (label) => ({ level: label.toUpperCase() }),
      },
    },
    destination,
  );
}

export const logger = createLogger();

/** Operational endpoints, polled every few seconds by Kubernetes and Prometheus. */
const UNLOGGED_PATHS = new Set(["/health", "/metrics"]);

/** Drops the query string, which may carry tokens or personal data. */
function pathOf(url: string | undefined): string {
  return (url ?? "").split("?")[0];
}

/**
 * Logs one line per handled request, once the response has been sent.
 *
 * Only the method, path and status are recorded. Headers and bodies are left
 * out on purpose: they carry the auth cookie and user credentials, which must
 * not reach the log store.
 */
export function createHttpLogger(log: pino.Logger = logger) {
  return pinoHttp({
    logger: log,
    autoLogging: {
      ignore: (req: IncomingMessage) => UNLOGGED_PATHS.has(pathOf(req.url)),
    },
    serializers: {
      req: (req: IncomingMessage) => ({ method: req.method, path: pathOf(req.url) }),
      res: (res: ServerResponse) => ({ status: res.statusCode }),
    },
  });
}

export const httpLogger = createHttpLogger();
