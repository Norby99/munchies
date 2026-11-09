// Must stay first: it patches http, express and pino as they are loaded (see tracing.ts).
import "./infrastructure/adapter/middleware/tracing";
import type { Server } from "node:http";
import express from "express";
import expressListEndpoints from "express-list-endpoints";
import cookieParser from "cookie-parser";
import jwt from "jsonwebtoken";
import { applyRoutes } from "./infrastructure/adapter/middleware/routes/routes";
import {
  metricsHandler,
  metricsMiddleware,
} from "./infrastructure/adapter/middleware/metrics";
import { httpLogger, logger } from "./infrastructure/adapter/middleware/logger";
async function main(): Promise<void> {
  const app = express();
  app.use(express.raw({ type: "application/json", limit: '1mb'}));
  app.use(cookieParser());
  app.use(metricsMiddleware());
  app.use(httpLogger);

  app.get("/health", (_req, res) => {
    res.status(200).json({ status: "UP" });
  });
  app.get("/metrics", metricsHandler);

  const JWT_SECRET = process.env.JWT_SECRET ?? "blastimus_prime";

  app.get("/auth/token", (_req, res) => {
    const token = jwt.sign(
      { sub: "benchmark-user", id: "benchmark-user", role: "CUSTOMER" },
      JWT_SECRET,
      { expiresIn: "7d" }
    );
    res.status(200).json({ token });
  });

  app.get("/auth/verify", (req, res) => {
    const authHeader = req.headers.authorization;
    const bearerToken = authHeader?.startsWith("Bearer ") ? authHeader.slice(7) : undefined;
    const token = (req.cookies?.authToken as string | undefined) ?? bearerToken ?? (req.query?.token as string | undefined);

    if (!token) {
      res.status(401).json({ status: "UNAUTHORIZED", error: "missing token" });
      return;
    }

    try {
      const decoded = jwt.verify(token, JWT_SECRET);
      res.status(200).json({ status: "VALID", user: decoded });
    } catch (err: any) {
      res.status(401).json({ status: "INVALID", error: err.message });
    }
  });

  applyRoutes(app);

  const PORT = process.env.PORT ?? 8080;
  const server = app.listen(PORT, () => {
    logger.info({ port: PORT }, "Gateway server online");

    expressListEndpoints(app).forEach(({ methods, path }) => {
      logger.debug({ methods, path }, "Route registered");
    });
  });

  // Kubernetes balances per connection, not per request: a keep-alive connection stays on the
  // pod it was opened to, so replicas added by the autoscaler get no share of the traffic from
  // clients that are already connected. Closing each connection after a fixed number of
  // requests (with `Connection: close` on the last response) makes clients reconnect, and the
  // new connection goes to any ready pod. A low value rebalances within seconds under load;
  // the cost is one TCP handshake per that many requests, negligible inside the cluster.
  server.maxRequestsPerSocket = MAX_REQUESTS_PER_SOCKET;

  handleShutdown(server);
}

const MAX_REQUESTS_PER_SOCKET = 20;
// Upper bound on draining: with the chart's 10 s preStop sleep it stays below Kubernetes'
// default 30 s grace period, after which the pod is killed.
const SHUTDOWN_TIMEOUT_MS = 15_000;

/**
 * Graceful shutdown on SIGTERM, sent by Kubernetes when a pod is removed (for example when the
 * autoscaler scales in). By then the chart's preStop sleep has given the cluster time to take
 * the pod out of the Service, so no new connections arrive. Closing the server stops accepting
 * connections, closes idle keep-alive ones and lets in-flight requests finish before the process
 * exits; without this, Node's default SIGTERM behaviour exits at once and resets them.
 */
function handleShutdown(server: Server): void {
  process.once("SIGTERM", () => {
    logger.info("SIGTERM received, closing the server");
    const forceExit = setTimeout(() => {
      logger.warn("Shutdown timed out, closing the remaining connections");
      server.closeAllConnections();
      process.exit(0);
    }, SHUTDOWN_TIMEOUT_MS);
    forceExit.unref();

    server.close(() => {
      logger.info("Server closed");
      process.exit(0);
    });
  });
}

main();
