import { Counter, Gauge, Registry, collectDefaultMetrics } from "prom-client";

export const metricsRegistry = new Registry();

collectDefaultMetrics({ register: metricsRegistry });

export const notificationsReceived = new Counter({
  name: "notifications_received_total",
  help: "Notification events consumed from Kafka, by notification type",
  labelNames: ["type"],
  registers: [metricsRegistry],
});

export const notificationsPushed = new Counter({
  name: "notifications_pushed_total",
  help: "Live notifications emitted to WebSocket rooms, by notification type",
  labelNames: ["type"],
  registers: [metricsRegistry],
});

export const liveConnections = new Gauge({
  name: "live_connections",
  help: "Open live-notification (WebSocket) connections",
  registers: [metricsRegistry],
});
