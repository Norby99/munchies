import "./setup-env";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { createServer, type IncomingHttpHeaders, type Server as HttpServer } from "node:http";
import type { AddressInfo } from "node:net";
import jwt from "jsonwebtoken";
import { Server as SocketIoServer } from "socket.io";
import { io as connect, type Socket as ClientSocket } from "socket.io-client";
import { ID_CLAIM, ROLE_CLAIM } from "munchies-commons/kotlin/commons-modules";
import { LiveNotificationsConfig } from "munchies-notification-service-shared/kotlin/notification-modules";
import {
  attachLiveNotifications,
  type ManagerRestaurants,
} from "../../main/ts/infrastructure/adapter/middleware/live-notifications";

const listen = (server: HttpServer) =>
  new Promise<string>((resolve) =>
    server.listen(0, () => resolve(`http://localhost:${(server.address() as AddressInfo).port}`))
  );

const cookieFor = (id: string, role: string) =>
  `authToken=${jwt.sign({ sub: id, [ID_CLAIM]: id, [ROLE_CLAIM]: role }, process.env.JWT_SECRET!)}`;

/**
 * The gateway's WebSocket proxy, between a real Socket.io client and a stand-in for
 * notification-service (a Socket.io server recording the handshake headers it receives).
 */
describe("live notifications proxy", () => {
  let upstream: HttpServer;
  let upstreamIo: SocketIoServer;
  let gateway: HttpServer;
  let gatewayUrl: string;
  let handshakes: IncomingHttpHeaders[];
  let restaurantsOf: ManagerRestaurants;
  const clients: ClientSocket[] = [];

  beforeEach(async () => {
    handshakes = [];
    upstream = createServer();
    upstreamIo = new SocketIoServer(upstream, { path: LiveNotificationsConfig.PATH, transports: ["websocket"] });
    upstreamIo.on("connection", (socket) => handshakes.push(socket.handshake.headers));
    const upstreamUrl = await listen(upstream);

    restaurantsOf = async () => ["r1", "r2"];
    gateway = createServer((_req, res) => res.end());
    attachLiveNotifications(gateway, { target: upstreamUrl, restaurantsOf: (id) => restaurantsOf(id) });
    gatewayUrl = await listen(gateway);
  });

  afterEach(async () => {
    clients.splice(0).forEach((c) => c.disconnect());
    await upstreamIo.close();
    gateway.closeAllConnections();
    await new Promise((resolve) => gateway.close(resolve));
  });

  function client(headers: Record<string, string> = {}): ClientSocket {
    const socket = connect(gatewayUrl, {
      path: LiveNotificationsConfig.PATH,
      transports: ["websocket"],
      extraHeaders: headers,
      reconnection: false,
    });
    clients.push(socket);
    return socket;
  }

  const connected = (socket: ClientSocket) =>
    new Promise<void>((resolve, reject) => {
      socket.once("connect", () => resolve());
      socket.once("connect_error", reject);
    });

  it("refuses a connection without the session cookie", async () => {
    await expect(connected(client())).rejects.toThrow();
    expect(handshakes).toHaveLength(0);
  });

  it("refuses a connection with an invalid token", async () => {
    await expect(connected(client({ cookie: "authToken=not-a-jwt" }))).rejects.toThrow();
    expect(handshakes).toHaveLength(0);
  });

  it("forwards a customer's identity and drops identity headers sent by the client", async () => {
    await connected(
      client({
        cookie: cookieFor("customer-1", "CUSTOMER"),
        [LiveNotificationsConfig.USER_ID_HEADER]: "someone-else",
        [LiveNotificationsConfig.RESTAURANT_IDS_HEADER]: "not-mine",
      })
    );

    expect(handshakes).toHaveLength(1);
    expect(handshakes[0][LiveNotificationsConfig.USER_ID_HEADER]).toBe("customer-1");
    expect(handshakes[0][LiveNotificationsConfig.USER_ROLE_HEADER]).toBe("CUSTOMER");
    expect(handshakes[0][LiveNotificationsConfig.RESTAURANT_IDS_HEADER]).toBeUndefined();
  });

  it("forwards the restaurants a manager owns", async () => {
    await connected(client({ cookie: cookieFor("manager-1", "MANAGER") }));

    expect(handshakes[0][LiveNotificationsConfig.USER_ROLE_HEADER]).toBe("MANAGER");
    expect(handshakes[0][LiveNotificationsConfig.RESTAURANT_IDS_HEADER]).toBe("r1,r2");
  });

  it("refuses a manager whose restaurants cannot be resolved, so the client retries", async () => {
    restaurantsOf = async () => null;

    await expect(connected(client({ cookie: cookieFor("manager-1", "MANAGER") }))).rejects.toThrow();
    expect(handshakes).toHaveLength(0);
  });

  it("relays notifications from notification-service to the client", async () => {
    const socket = client({ cookie: cookieFor("customer-1", "CUSTOMER") });
    await connected(socket);
    const received = new Promise((resolve) => socket.once(LiveNotificationsConfig.EVENT, resolve));

    upstreamIo.emit(LiveNotificationsConfig.EVENT, { type: "ORDER_CHANGED", orderId: "o1" });

    await expect(received).resolves.toEqual({ type: "ORDER_CHANGED", orderId: "o1" });
  });
});
