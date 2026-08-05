import { HttpMethod } from "munchies-commons/kotlin/commons-modules";
import { RouteDefinition } from "./route-definition";
import { SimpleRoute } from "./simple-route";
import { userRoutes } from "./user/user.routes";
import { restaurantRoutes } from "./restaurant/restaurant.routes";
import { orderRoutes } from "./order/order.routes";
import { Express, NextFunction, Request, Response } from "express";

const routes: SimpleRoute<any>[] = [
  ...userRoutes,
  ...restaurantRoutes,
  ...orderRoutes,
];


export function fillPath(path: string, ...values: (string | number)[]): string {
  let i = 0;
  return path.replace(/\{([^}]+)\}/g, () => {
    if (i >= values.length) {
      throw new Error("Not enough values provided for path params");
    }
    return String(values[i++]);
  });
}

function convertRouteToExpress(path: string): string {
  return path.replace(/\{([^}]+)\}/g, ":$1");
}

import { RequestHandler } from "express";
import { requireAuth, requireRole } from "../auth";
function createRoute(
  app: Express,
  path: string,
  method: HttpMethod,
  ...handlers: RequestHandler[]
): Express {
  switch (method.name) {
    case HttpMethod.POST.name:
      return app.post(convertRouteToExpress(path), ...handlers);
    case HttpMethod.DELETE.name:
      return app.delete(convertRouteToExpress(path), ...handlers);
    case HttpMethod.PATCH.name:
      return app.patch(convertRouteToExpress(path), ...handlers);
    default:
      return app.get(convertRouteToExpress(path), ...handlers);
  }
}

function logRequests(path: string, method: string): RequestHandler {
  return async (req: Request, res: Response, next: NextFunction) => {
    console.log("[ " + method.toUpperCase() + " | " + path + " ] => ", req.body);
    next();
  };
}

type AsyncHandler = (
  req: Request,
  res: Response,
  next: NextFunction
) => unknown | Promise<unknown>;

export function catchError(handler: AsyncHandler): RequestHandler {
  return (req: Request, res: Response, next: NextFunction) => {
    Promise.resolve(handler(req, res, next)).catch(next);
  };
}
export function applyRoutes(app: Express) {
  for (const r of routes) {
    const method = r.method as HttpMethod;
    const path = r.path;
    const handlers: RequestHandler[] = [
      logRequests(path, method.name.toString())
    ];
    
    if (r.authRole) {
      handlers.push(requireAuth());
      handlers.push(requireRole(r.authRole));
    }
    handlers.push(catchError(r.respond));
    createRoute(app, path, method, ...handlers);
  }
}
