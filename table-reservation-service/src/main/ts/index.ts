import "dotenv/config";
// Must stay ahead of every other import: it patches http, express, kafkajs and pino as they
// are loaded (see tracing.ts). Only dotenv comes first, so the OTLP endpoint can be set in .env.
import "@main/infrastructure/adapter/outbound/tracing/tracing";
import express, { Express, Request, Response, NextFunction } from "express";
import { ErrorResponse } from "munchies-commons/kotlin/commons-modules";
import {
  TableReservationResponse,
  TableReservationServiceConfig,
  TableReservationsResponse,
  cancelTableReservationRequestFromJson,
  modifyTableReservationRequestFromJson,
  reserveTableRequestFromJson,
} from "munchies-table-reservation-service-shared/kotlin/table-reservation-modules";
import {
  ReservationError,
  TableReservationController,
} from "@main/infrastructure/adapter/inbound/web/controller/controller";
import {
  metricsHandler,
  metricsMiddleware,
} from "@main/infrastructure/adapter/inbound/web/config/metrics";
import {
  connectDB,
  disconnectDB,
} from "@main/infrastructure/adapter/outbound/mongo/config/db";
import { TableReservationModel } from "@main/infrastructure/adapter/outbound/mongo/document/table-reservation-document";
import {
  httpLogger,
  logger,
} from "@main/infrastructure/adapter/outbound/logging/logger";

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

/** A request the service cannot even read: a body that does not decode as its contract. */
class MalformedRequestError extends Error {}

/** Decodes a request body with the shared parser of its contract. */
function decode<T>(req: Request, fromJson: (json: string) => T): T {
  try {
    return fromJson(parseBodyToString(req.body));
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : String(error);
    throw new MalformedRequestError("Malformed request body: " + message);
  }
}

/** A query parameter as a string, or undefined when missing or empty. */
function queryParam(req: Request, name: string): string | undefined {
  const value = req.query[name];
  return typeof value === "string" && value !== "" ? value : undefined;
}

type Handler = (req: Request, res: Response) => Promise<void>;

/** Routes a rejected handler to the error middleware (Express 4 does not await handlers). */
function route(handler: Handler) {
  return (req: Request, res: Response, next: NextFunction) => {
    handler(req, res).catch(next);
  };
}

const { SERVICE_PATH, CUSTOMER_ID_QUERY, RESTAURANT_ID_QUERY, MANAGER_ID_QUERY } =
  TableReservationServiceConfig;

/** `{id}` placeholders of the shared paths, in Express syntax, without the trailing slash. */
function expressPath(path: string): string {
  return (SERVICE_PATH + path).replace(/\{([^}]+)\}/g, ":$1").replace(/\/$/, "");
}

/**
 * Builds the HTTP application. Paths come from table-reservation-shared, the same constants
 * the gateway and order-service build their calls from. Every answer is the `{ result, code }`
 * envelope the other Munchies services use: a reservation (or a list) on success, the reason
 * on failure.
 */
export function createApp(
  controller: TableReservationController = new TableReservationController(),
): Express {
  const app = express();
  app.use(express.raw({ type: "application/json", limit: "1mb" }));
  app.use(express.json());
  app.use(metricsMiddleware());
  app.use(httpLogger);

  app.get("/health", (_req: Request, res: Response) => {
    res.status(200).json({ status: "UP" });
  });
  app.get("/metrics", metricsHandler);

  app.post(
    expressPath(TableReservationServiceConfig.RESERVE_TABLE_PATH),
    route(async (req, res) => {
      const request = decode(req, reserveTableRequestFromJson);
      const reservation = await controller.reserveTable(request);
      res.status(201).type("json").send(new TableReservationResponse(reservation, 201).toJson());
    }),
  );

  app.get(
    expressPath(""),
    route(async (req, res) => {
      const reservations = await controller.getReservations(
        queryParam(req, CUSTOMER_ID_QUERY),
        queryParam(req, RESTAURANT_ID_QUERY),
        queryParam(req, MANAGER_ID_QUERY),
      );
      res.status(200).type("json").send(new TableReservationsResponse(reservations, 200).toJson());
    }),
  );

  app.get(
    expressPath(TableReservationServiceConfig.GET_RESERVATION_PATH),
    route(async (req, res) => {
      const reservation = await controller.getReservation(
        req.params.id,
        queryParam(req, CUSTOMER_ID_QUERY),
      );
      res.status(200).type("json").send(new TableReservationResponse(reservation, 200).toJson());
    }),
  );

  app.put(
    expressPath(TableReservationServiceConfig.MODIFY_RESERVATION_PATH),
    route(async (req, res) => {
      const request = decode(req, modifyTableReservationRequestFromJson);
      const reservation = await controller.modifyReservation(req.params.id, request);
      res.status(200).type("json").send(new TableReservationResponse(reservation, 200).toJson());
    }),
  );

  app.patch(
    expressPath(TableReservationServiceConfig.CANCEL_RESERVATION_PATH),
    route(async (req, res) => {
      const request = decode(req, cancelTableReservationRequestFromJson);
      const reservation = await controller.cancelReservation(req.params.id, request);
      res.status(200).type("json").send(new TableReservationResponse(reservation, 200).toJson());
    }),
  );

  app.use((err: unknown, _req: Request, res: Response, _next: NextFunction) => {
    const message = err instanceof Error ? err.message : String(err);
    let status = 500;
    if (err instanceof ReservationError) status = err.status;
    else if (err instanceof MalformedRequestError) status = 400;
    else logger.error({ err }, "Unexpected error while handling a request");
    const errorResponse = new ErrorResponse(message, status);
    res.status(errorResponse.code).type("json").send(errorResponse.toJson());
  });

  return app;
}

async function main(): Promise<void> {
  if (process.env.MONGODB_URI) {
    // No fallback to the in-memory store: reservations would silently vanish on restart and
    // differ between replicas. Without its database the service must not start.
    await connectDB();
    // Builds the indexes before the first request: the unique one is what prevents double
    // booking (see table-reservation-document).
    await TableReservationModel.init();
    logger.info("Connected to MongoDB");
  } else {
    logger.warn("MONGODB_URI is not set: reservations are kept in memory");
  }

  const app = createApp();
  const PORT = process.env.PORT ?? TableReservationServiceConfig.SERVICE_PORT ?? 8080;
  const server = app.listen(PORT, () => {
    logger.info({ port: PORT }, "Table reservation service online");
  });

  const shutdown = async () => {
    logger.info("Shutting down table reservation service");
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

// Importing this module (tests do, for createApp) must not start the server.
if (require.main === module) {
  main().catch((err: unknown) => {
    logger.error({ err }, "Fatal error during startup");
    process.exit(1);
  });
}
