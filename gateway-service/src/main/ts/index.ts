// Must stay first: it patches http, express and pino as they are loaded (see tracing.ts).
import "./infrastructure/adapter/middleware/tracing";
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
  app.listen(PORT, () => {
    logger.info({ port: PORT }, "Gateway server online");

    expressListEndpoints(app).forEach(({ methods, path }) => {
      logger.debug({ methods, path }, "Route registered");
    });
  });
}

main();
