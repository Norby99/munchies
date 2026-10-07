import { Counter, Registry, collectDefaultMetrics } from "prom-client";

export const metricsRegistry = new Registry();

collectDefaultMetrics({ register: metricsRegistry });

export const notificationsReceived = new Counter({
  name: "notifications_received_total",
  help: "Notification events consumed from Kafka, by notification type",
  labelNames: ["type"],
  registers: [metricsRegistry],
});
