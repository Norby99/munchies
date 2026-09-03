import express, { Express, Request, Response, NextFunction } from "express";
import "dotenv/config";
import { ErrorResponse } from "munchies-commons/kotlin/commons-modules";
import { PaymentController } from "@main/infrastructure/adapter/inbound/web/controller/controller";
import {
  processPaymentRequestFromJson,
  PaymentServiceConfig,
} from "munchies-payment-service-shared/kotlin/payment-modules";
import {
  connectDB,
  disconnectDB,
} from "@main/infrastructure/adapter/outbound/mongo/config/db";

export function parseBodyToString(body: unknown): string {
  if (typeof body === "string") {
    return body;
  }
  if (Buffer.isBuffer(body)) {
    return body.toString("utf-8");
  }
  if (body !== null && typeof body === "object") {
    return JSON.stringify(body);
  }
  return String(body ?? "");
}

export function createApp(
  controller: PaymentController = new PaymentController()
): Express {
  const app = express();
  app.use(express.raw({ type: "application/json", limit: "1mb" }));
  app.use(express.json());

  app.get("/health", (_req: Request, res: Response) => {
    res.status(200).json({ status: "UP" });
  });

  app.post("/payments", async (req: Request, res: Response) => {
    try {
      const rawBody = String(req.body);
      const request = processPaymentRequestFromJson(rawBody);
      const response = await controller.processPayment(request);
      res.status(200).type("json").send(response.toJson());
    } catch (error: unknown) {
      const message =
        error instanceof Error ? error.message : String(error);
      const errorResponse = new ErrorResponse(message, 400);
      res.status(errorResponse.code).type("json").send(errorResponse.toJson());
    }
  });

  app.use(
    (err: unknown, _req: Request, res: Response, _next: NextFunction) => {
      const message = err instanceof Error ? err.message : String(err);
      const errorResponse = new ErrorResponse(message, 500);
      res.status(errorResponse.code).type("json").send(errorResponse.toJson());
    }
  );

  return app;
}

async function main(): Promise<void> {
  if (process.env.MONGODB_URI) {
    try {
      await connectDB();
      console.log("Connected to MongoDB");
    } catch (error) {
      console.warn(
        "MongoDB connection failed, falling back to in-memory mode:",
        error
      );
    }
  }

  const app = createApp();
  const PORT = process.env.PORT ?? PaymentServiceConfig.SERVICE_PORT ?? 8080;
  const server = app.listen(PORT, () => {
    console.log(`Payment service online on port ${PORT}`);
  });

  const shutdown = async () => {
    console.log("Shutting down payment service...");
    server.close(async () => {
      try {
        await disconnectDB();
      } catch {
        // ignore on shutdown
      }
      process.exit(0);
    });
  };

  process.on("SIGINT", shutdown);
  process.on("SIGTERM", shutdown);
}

void main();
