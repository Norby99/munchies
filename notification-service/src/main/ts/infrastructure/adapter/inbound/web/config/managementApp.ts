import express, { Express, Request, Response } from "express";
import { metricsRegistry } from "@main/infrastructure/adapter/inbound/web/config/metrics";

export function createManagementApp(): Express {
  const app = express();

  app.get("/health", (_req: Request, res: Response) => {
    res.status(200).json({ status: "UP" });
  });

  app.get("/metrics", async (_req: Request, res: Response) => {
    res
      .status(200)
      .set("Content-Type", metricsRegistry.contentType)
      .send(await metricsRegistry.metrics());
  });

  return app;
}
