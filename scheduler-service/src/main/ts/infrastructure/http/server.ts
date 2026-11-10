import express, { Express, Request, Response } from "express";
import { GetAvailability } from "../../application/getAvailability";
import { GetLoad } from "../../application/getLoad";
import { ScheduleOrder } from "../../application/scheduleOrder";
import { AdvanceScheduledOrder } from "../../application/advanceScheduledOrder";
import { OrderType } from "../../domain/model/booking";
import { GeoPoint } from "../../domain/model/geo";
import { badRequest, notFound } from "../../application/errors";
import { GetBooking } from "../../application/getBooking";
import { fail, ok } from "./envelope";

export interface Handlers {
  getAvailability: GetAvailability;
  getLoad: GetLoad;
  scheduleOrder: ScheduleOrder;
  advanceScheduledOrder: AdvanceScheduledOrder;
  getBooking: GetBooking;
}

const asyncRoute =
  (fn: (req: Request, res: Response) => Promise<void>) => (req: Request, res: Response) => {
    fn(req, res).catch((err) => fail(res, err));
  };

function parseLatLng(lat: unknown, lng: unknown): GeoPoint | undefined {
  if (lat === undefined || lng === undefined) return undefined;
  return { lat: Number(lat), lng: Number(lng) };
}

function requireQueryDate(req: Request): string {
  const date = req.query.date;
  if (typeof date !== "string" || date.length === 0) {
    throw badRequest("query parameter 'date' (YYYY-MM-DD) is required");
  }
  return date;
}

export function buildApp(h: Handlers): Express {
  const app = express();
  app.use(express.json({ limit: "1mb" }));

  app.get("/health", (_req, res) => res.status(200).json({ result: "ok", code: 200 }));

  app.get(
    "/schedule/:restaurantId/availability",
    asyncRoute(async (req, res) => {
      const result = await h.getAvailability.execute({
        restaurantId: req.params.restaurantId,
        dateIso: requireQueryDate(req),
        orderType: (req.query.orderType as OrderType | undefined) || undefined,
        deliveryLocation: parseLatLng(req.query.lat, req.query.lng),
      });
      ok(res, result);
    }),
  );

  app.get(
    "/schedule/:restaurantId/load",
    asyncRoute(async (req, res) => {
      const result = await h.getLoad.execute(req.params.restaurantId, requireQueryDate(req));
      ok(res, result);
    }),
  );

  app.post(
    "/schedule/:restaurantId/orders",
    asyncRoute(async (req, res) => {
      const body = req.body ?? {};
      const result = await h.scheduleOrder.execute({
        restaurantId: req.params.restaurantId,
        customerId: body.customerId,
        items: body.items,
        orderType: body.orderType,
        slotStartEpochMs: Number(body.slotStartEpochMs),
        deliveryAddress: body.deliveryAddress,
        deliveryLocation: body.deliveryLocation,
        bellName: body.bellName,
        customerPhone: body.customerPhone,
        customerName: body.customerName,
        tableNumber: body.tableNumber,
        numberOfGuests: body.numberOfGuests,
      });
      ok(res, result, 201);
    }),
  );

  app.get(
    "/schedule/bookings/:bookingId",
    asyncRoute(async (req, res) => {
      const booking = await h.getBooking.execute(req.params.bookingId);
      if (!booking) throw notFound("booking not found");
      ok(res, booking);
    }),
  );

  app.post(
    "/schedule/orders/:orderId/advance",
    asyncRoute(async (req, res) => {
      const result = await h.advanceScheduledOrder.execute(req.params.orderId);
      ok(res, result);
    }),
  );

  app.use((_req, res) => res.status(404).json({ result: "No such scheduler route", code: 404 }));

  return app;
}
