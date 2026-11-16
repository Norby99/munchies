import type { Server as HttpServer } from "node:http";
import { Server, type Socket } from "socket.io";
import { LiveNotificationsConfig } from "munchies-notification-service-shared/kotlin/notification-modules";
import type { LiveNotification } from "@main/domain/model/LiveNotification";
import type { LiveNotifier } from "@main/domain/port/LiveNotifier";
import { liveConnections, notificationsPushed } from "@main/infrastructure/adapter/inbound/web/config/metrics";
import { logger } from "@main/infrastructure/adapter/outbound/logging/logger";

/** Room of every connection of one user. */
export const userRoom = (userId: string): string => `user:${userId}`;

/** Room of every connection of the staff of one restaurant. */
export const restaurantRoom = (restaurantId: string): string => `restaurant:${restaurantId}`;

/** The identity gateway-service forwarded for a connection. */
interface ConnectionIdentity {
  userId: string;
  role: string;
  restaurantIds: string[];
}

function header(socket: Socket, name: string): string | undefined {
  const value = socket.handshake.headers[name];
  return Array.isArray(value) ? value[0] : value;
}

/**
 * Reads the identity gateway-service attached to the handshake (see LiveNotificationsConfig).
 * notification-service is only reachable through the gateway, which authenticates the session
 * cookie and overwrites these headers, so they can be trusted here.
 */
export function identityOf(socket: Socket): ConnectionIdentity | null {
  const userId = header(socket, LiveNotificationsConfig.USER_ID_HEADER);
  const role = header(socket, LiveNotificationsConfig.USER_ROLE_HEADER);
  if (!userId || !role) return null;
  const restaurantIds = (header(socket, LiveNotificationsConfig.RESTAURANT_IDS_HEADER) ?? "")
    .split(",")
    .map((id) => id.trim())
    .filter((id) => id.length > 0);
  return { userId, role, restaurantIds };
}

/**
 * The live-notification channel, as a Socket.io server sharing notification-service's HTTP
 * server (the management one: health and metrics stay on their own paths).
 *
 * It is both ends of the WebSocket: inbound, it accepts the connections gateway-service proxies
 * and puts each one in the rooms of its user and, for a manager, of the restaurants they own;
 * outbound, it implements {@link LiveNotifier} by emitting to those rooms. Rooms make the fan-out
 * a single `emit`, whatever the number of tabs or devices a user has open.
 *
 * Rooms live in this process's memory: with more than one replica, a connection and the Kafka
 * partition carrying its events could end up on different pods. notification-service therefore
 * runs as a single replica (k8s/notification-service/deployment.yml); scaling it out would need
 * a shared Socket.io adapter (e.g. Redis) or one consumer group per replica.
 *
 * Only WebSocket transport is accepted: the gateway proxies the upgrade request, and the
 * frontend's client is configured the same way, so there is no HTTP long-polling fallback.
 */
export class SocketIoLiveNotifier implements LiveNotifier {
  private readonly io: Server;

  constructor(httpServer: HttpServer) {
    this.io = new Server(httpServer, {
      path: LiveNotificationsConfig.PATH,
      serveClient: false,
      transports: ["websocket"],
    });

    // Refuse connections that did not come through the gateway's authentication.
    this.io.use((socket, next) => {
      const identity = identityOf(socket);
      if (!identity) {
        logger.warn("Rejected live connection without a forwarded identity");
        next(new Error("unauthenticated"));
        return;
      }
      socket.data.identity = identity;
      next();
    });

    this.io.on("connection", (socket) => {
      const identity = socket.data.identity as ConnectionIdentity;
      const rooms = [userRoom(identity.userId), ...identity.restaurantIds.map(restaurantRoom)];
      void socket.join(rooms);
      liveConnections.inc();
      logger.info(
        { userId: identity.userId, role: identity.role, restaurants: identity.restaurantIds.length },
        "Live connection opened"
      );
      socket.on("disconnect", () => {
        liveConnections.dec();
      });
    });
  }

  async toUser(userId: string, notification: LiveNotification): Promise<void> {
    this.emit(userRoom(userId), notification);
  }

  async toRestaurant(restaurantId: string, notification: LiveNotification): Promise<void> {
    this.emit(restaurantRoom(restaurantId), notification);
  }

  /** Closes every connection; used on shutdown and by tests. */
  async close(): Promise<void> {
    await this.io.close();
  }

  // emit() only queues the frame on each socket: it never waits on the network.
  private emit(room: string, notification: LiveNotification): void {
    this.io.to(room).emit(LiveNotificationsConfig.EVENT, notification);
    notificationsPushed.inc({ type: notification.type });
  }
}
