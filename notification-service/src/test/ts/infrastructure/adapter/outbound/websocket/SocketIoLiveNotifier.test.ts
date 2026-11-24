import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { createServer, type Server as HttpServer } from "node:http";
import type { AddressInfo } from "node:net";
import { io as connect, type Socket as ClientSocket } from "socket.io-client";
import { LiveNotificationsConfig } from "munchies-notification-service-shared/kotlin/notification-modules";
import { SocketIoLiveNotifier } from "@main/infrastructure/adapter/outbound/websocket/SocketIoLiveNotifier";
import type { LiveNotification } from "@main/domain/model/LiveNotification";

/**
 * Integration test of the live-notification channel over a real Socket.io connection: clients
 * connect the way gateway-service forwards them (identity in the handshake headers).
 */
describe("SocketIoLiveNotifier", () => {
  let server: HttpServer;
  let notifier: SocketIoLiveNotifier;
  let url: string;
  const clients: ClientSocket[] = [];

  beforeEach(async () => {
    server = createServer();
    notifier = new SocketIoLiveNotifier(server);
    await new Promise<void>((resolve) => server.listen(0, resolve));
    url = `http://localhost:${(server.address() as AddressInfo).port}`;
  });

  afterEach(async () => {
    clients.splice(0).forEach((c) => c.disconnect());
    await notifier.close();
  });

  function client(headers: Record<string, string>): ClientSocket {
    const socket = connect(url, {
      path: LiveNotificationsConfig.PATH,
      transports: ["websocket"],
      extraHeaders: headers,
      reconnection: false,
    });
    clients.push(socket);
    return socket;
  }

  const customer = (id: string) => ({
    [LiveNotificationsConfig.USER_ID_HEADER]: id,
    [LiveNotificationsConfig.USER_ROLE_HEADER]: "CUSTOMER",
  });
  const manager = (id: string, restaurants: string[]) => ({
    [LiveNotificationsConfig.USER_ID_HEADER]: id,
    [LiveNotificationsConfig.USER_ROLE_HEADER]: "MANAGER",
    [LiveNotificationsConfig.RESTAURANT_IDS_HEADER]: restaurants.join(","),
  });

  const connected = (socket: ClientSocket) =>
    new Promise<void>((resolve, reject) => {
      socket.once("connect", () => resolve());
      socket.once("connect_error", reject);
    });

  /** Collects what a socket receives during a short window. */
  async function received(socket: ClientSocket, act: () => Promise<void>): Promise<LiveNotification[]> {
    const got: LiveNotification[] = [];
    socket.on(LiveNotificationsConfig.EVENT, (n: LiveNotification) => got.push(n));
    await act();
    await new Promise((r) => setTimeout(r, 150));
    return got;
  }

  const order = (customerId: string, restaurantId: string): LiveNotification => ({
    type: "ORDER_CHANGED",
    orderId: "order-1",
    restaurantId,
    customerId,
    status: "PREPARING",
    payed: true,
  });

  it("refuses a connection without a forwarded identity", async () => {
    await expect(connected(client({}))).rejects.toThrow("unauthenticated");
  });

  it("delivers a user's notification to that user only", async () => {
    const alice = client(customer("alice"));
    const bob = client(customer("bob"));
    await Promise.all([connected(alice), connected(bob)]);

    const [aliceGot, bobGot] = await Promise.all([
      received(alice, () => notifier.toUser("alice", order("alice", "r1"))),
      received(bob, async () => {}),
    ]);

    expect(aliceGot).toEqual([order("alice", "r1")]);
    expect(bobGot).toEqual([]);
  });

  it("delivers to every tab of the same user", async () => {
    const tab1 = client(customer("alice"));
    const tab2 = client(customer("alice"));
    await Promise.all([connected(tab1), connected(tab2)]);

    const [got1, got2] = await Promise.all([
      received(tab1, () => notifier.toUser("alice", order("alice", "r1"))),
      received(tab2, async () => {}),
    ]);

    expect(got1).toHaveLength(1);
    expect(got2).toHaveLength(1);
  });

  it("delivers a restaurant's notification to the managers of that restaurant only", async () => {
    const owner = client(manager("m1", ["r1", "r2"]));
    const other = client(manager("m2", ["r3"]));
    await Promise.all([connected(owner), connected(other)]);

    const [ownerGot, otherGot] = await Promise.all([
      received(owner, () => notifier.toRestaurant("r2", order("alice", "r2"))),
      received(other, async () => {}),
    ]);

    expect(ownerGot).toEqual([order("alice", "r2")]);
    expect(otherGot).toEqual([]);
  });
});
