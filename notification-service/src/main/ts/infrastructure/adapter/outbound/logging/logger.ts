import pino from "pino";

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
      base: { service: "notification-service" },
      timestamp: pino.stdTimeFunctions.isoTime,
      formatters: {
        level: (label) => ({ level: label.toUpperCase() }),
      },
    },
    destination,
  );
}

export const logger = createLogger();
