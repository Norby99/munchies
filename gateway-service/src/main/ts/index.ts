import express from "express";
import expressListEndpoints from "express-list-endpoints";
import cookieParser from "cookie-parser";
import { applyRoutes } from "./infrastructure/adapter/middleware/routes/routes";
import {
  metricsHandler,
  metricsMiddleware,
} from "./infrastructure/adapter/middleware/metrics";
async function main(): Promise<void> {
  const app = express();
  app.use(express.raw({ type: "application/json", limit: '1mb'}));
  app.use(cookieParser());
  app.use(metricsMiddleware());

  app.get("/health", (_req, res) => {
    res.status(200).json({ status: "UP" });
  });
  app.get("/metrics", metricsHandler);

  applyRoutes(app);

  const PORT = process.env.PORT ?? 8080;
  app.listen(PORT, () => {
    console.log("Gateway Server online");

    expressListEndpoints(app).forEach(({ methods, path }) => {
      console.log(`${methods.join(",").padEnd(10)} ${path}`);
    });
  });
}

main();
