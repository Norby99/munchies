import type { IncomingMessage, Server } from "node:http";
import type { Duplex } from "node:stream";
import httpProxy from "http-proxy";
import {
  AuthRole,
  DecodedTokenSuccess,
  ErrorResponse,
} from "munchies-commons/kotlin/commons-modules";
import { LiveNotificationsConfig } from "munchies-notification-service-shared/kotlin/notification-modules";
// auth.ts/token.ts and routes.ts/the route files import each other, and instantiate each other's
// classes at load time: loading auth.ts and routes.ts first (as index.ts does) lets the other side
// of each cycle finish loading before that happens.
import "./auth";
import "./routes/routes";
import { AuthTokenDecoder } from "./token";
import { logger } from "./logger";
import { GetManagerRestaurantsRoute } from "./routes/restaurant/get-manager-restaurants.route";

/** Resolves the restaurants a manager owns, to subscribe their connection to them. */
export type ManagerRestaurants = (managerId: string) => Promise<string[] | null>;

/** Default lookup: restaurant-service, through the same route the REST API uses. */
export const restaurantServiceLookup: ManagerRestaurants = async (managerId) => {
  const response = await new GetManagerRestaurantsRoute().getManagerRestaurants(managerId);
  if (response instanceof ErrorResponse) {
    logger.warn({ managerId, code: response.code }, "Could not resolve the manager's restaurants");
    return null;
  }
  return Array.from(response.result, (restaurant) => restaurant.id);
};

export interface LiveNotificationsOptions {
  /** Base URL of notification-service, e.g. http://notification-service:8080. */
  target: string;
  restaurantsOf?: ManagerRestaurants;
  decoder?: AuthTokenDecoder;
}

/**
 * Reads one cookie from a raw `Cookie` header. Upgrade requests do not go through Express, so
 * cookie-parser has not run on them.
 */
export function cookieValue(header: string | undefined, name: string): string | undefined {
  for (const pair of (header ?? "").split(";")) {
    const separator = pair.indexOf("=");
    if (separator < 0 || pair.slice(0, separator).trim() !== name) continue;
    try {
      return decodeURIComponent(pair.slice(separator + 1).trim());
    } catch {
      return undefined;
    }
  }
  return undefined;
}

/** Ends a refused upgrade with a plain HTTP status (the socket has not become a WebSocket yet). */
function refuse(socket: Duplex, status: number, reason: string): void {
  socket.end(`HTTP/1.1 ${status} ${reason}\r\nConnection: close\r\nContent-Length: 0\r\n\r\n`);
}

function isLiveNotificationsPath(url: string | undefined): boolean {
  const path = (url ?? "").split("?")[0];
  return path === LiveNotificationsConfig.PATH || path.startsWith(`${LiveNotificationsConfig.PATH}/`);
}

/**
 * The gateway's end of the live-notification channel (see LiveNotificationsConfig): it accepts
 * the frontend's WebSocket on {@link LiveNotificationsConfig.PATH} and proxies it, frames
 * untouched, to notification-service, which stays internal.
 *
 * Before proxying, the upgrade request is authenticated like any REST call, with the
 * `authToken` cookie, and the caller's identity is forwarded in headers: user id and role and,
 * for a manager, the restaurants they own (resolved here, once per connection, so that
 * notification-service never has to call another service). Identity headers sent by the client
 * are always removed first, so they cannot be forged.
 *
 * A manager who creates a restaurant while connected gets its notifications after the next
 * reconnection; the frontend reconnects when its list of restaurants changes.
 */
export function attachLiveNotifications(server: Server, options: LiveNotificationsOptions): void {
  const decoder = options.decoder ?? new AuthTokenDecoder();
  const restaurantsOf = options.restaurantsOf ?? restaurantServiceLookup;
  const proxy = httpProxy.createProxyServer({ target: options.target, ws: true });

  proxy.on("error", (err, _req, socket) => {
    logger.error({ err }, "Live-notification proxy error");
    if (socket && "destroy" in socket) (socket as Duplex).destroy();
  });

  server.on("upgrade", (req: IncomingMessage, socket: Duplex, head: Buffer) => {
    if (!isLiveNotificationsPath(req.url)) {
      refuse(socket, 404, "Not Found");
      return;
    }
    void authorizeAndProxy(req, socket, head).catch((err) => {
      logger.error({ err }, "Live-notification upgrade failed");
      refuse(socket, 500, "Internal Server Error");
    });
  });

  async function authorizeAndProxy(req: IncomingMessage, socket: Duplex, head: Buffer): Promise<void> {
    for (const name of [
      LiveNotificationsConfig.USER_ID_HEADER,
      LiveNotificationsConfig.USER_ROLE_HEADER,
      LiveNotificationsConfig.RESTAURANT_IDS_HEADER,
    ]) {
      delete req.headers[name];
    }

    const token = cookieValue(req.headers.cookie, "authToken");
    const decoded = token ? decoder.validateAndDecodeToken(token) : null;
    if (!(decoded instanceof DecodedTokenSuccess)) {
      logger.warn("Rejected live connection: missing or invalid auth token");
      refuse(socket, 401, "Unauthorized");
      return;
    }

    req.headers[LiveNotificationsConfig.USER_ID_HEADER] = decoded.id;
    req.headers[LiveNotificationsConfig.USER_ROLE_HEADER] = decoded.role.name;

    if (decoded.role.name === AuthRole.MANAGER.name) {
      const restaurantIds = await restaurantsOf(decoded.id);
      if (restaurantIds === null) {
        // Better to make the client retry than to connect a manager who would miss their orders.
        refuse(socket, 503, "Service Unavailable");
        return;
      }
      req.headers[LiveNotificationsConfig.RESTAURANT_IDS_HEADER] = restaurantIds.join(",");
    }

    proxy.ws(req, socket, head);
  }
}
