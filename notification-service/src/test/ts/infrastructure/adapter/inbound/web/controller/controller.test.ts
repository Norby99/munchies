import { afterEach, describe, expect, it, vi } from "vitest";
import { NotificationController } from "@main/infrastructure/adapter/inbound/web/controller/controller";
import { fakeNotifier } from "@test/fixtures/liveNotifier";
import {
  OrderStatusChangedNotification,
  PaymentSuccessNotification,
  TableReservationCanceledNotification,
  UserEmailConfirmationNotification,
} from "@main/domain/external-modules";
import { Currency } from "munchies-payment-service-shared/kotlin/payment-modules";
import { logger } from "@main/infrastructure/adapter/outbound/logging/logger";

describe("NotificationController", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("logs a user email-confirmation event instead of sending a notification", () => {
    const logSpy = vi.spyOn(logger, "info").mockImplementation(() => {});
    const controller = new NotificationController(fakeNotifier());
    const event = new UserEmailConfirmationNotification("user-1", "otk-123");

    controller.handleUserEmailConfirmation(event);

    expect(logSpy).toHaveBeenCalledTimes(1);
    const loggedMessage = JSON.stringify(logSpy.mock.calls[0]);
    expect(loggedMessage).toContain("UserEmailConfirmationNotification");
    expect(loggedMessage).toContain("user-1");
  });

  it("logs a payment-success event instead of sending a notification", () => {
    const logSpy = vi.spyOn(logger, "info").mockImplementation(() => {});
    const controller = new NotificationController(fakeNotifier());
    const event = new PaymentSuccessNotification(
      "payment-1",
      "order-1",
      100,
      Currency.EUR
    );

    controller.handlePaymentSuccess(event);

    expect(logSpy).toHaveBeenCalledTimes(1);
    const loggedMessage = JSON.stringify(logSpy.mock.calls[0]);
    expect(loggedMessage).toContain("PaymentSuccessNotification");
    expect(loggedMessage).toContain("payment-1");
  });

  it("logs an order status-change event instead of sending a notification", () => {
    const logSpy = vi.spyOn(logger, "info").mockImplementation(() => {});
    const controller = new NotificationController(fakeNotifier());
    const event = new OrderStatusChangedNotification(
      "order-1",
      "restaurant-1",
      "customer-1",
      "PREPARING",
      true
    );

    controller.handleOrderStatusChanged(event);

    expect(logSpy).toHaveBeenCalledTimes(1);
    const loggedMessage = JSON.stringify(logSpy.mock.calls[0]);
    expect(loggedMessage).toContain("OrderStatusChangedNotification");
    expect(loggedMessage).toContain("order-1");
  });

  it("pushes an order change to its customer and to its restaurant", async () => {
    vi.spyOn(logger, "info").mockImplementation(() => {});
    const notifier = fakeNotifier();
    const controller = new NotificationController(notifier);

    await controller.handleOrderStatusChanged(
      new OrderStatusChangedNotification("order-1", "restaurant-1", "customer-1", "READY", true)
    );

    const expected = {
      type: "ORDER_CHANGED",
      orderId: "order-1",
      restaurantId: "restaurant-1",
      customerId: "customer-1",
      status: "READY",
      payed: true,
    };
    expect(notifier.toUser).toHaveBeenCalledWith("customer-1", expected);
    expect(notifier.toRestaurant).toHaveBeenCalledWith("restaurant-1", expected);
  });

  it("pushes a reservation change with the whole reservation, to its customer and its restaurant", async () => {
    vi.spyOn(logger, "info").mockImplementation(() => {});
    const notifier = fakeNotifier();
    const controller = new NotificationController(notifier);

    await controller.handleTableReservationCanceled(
      new TableReservationCanceledNotification(
        "reservation-1",
        "restaurant-1",
        "customer-1",
        "2026-11-14T19:00:00Z",
        "2026-11-14T21:00:00Z",
        4,
        2
      )
    );

    const expected = {
      type: "RESERVATION_CHANGED",
      change: "CANCELED",
      reservationId: "reservation-1",
      restaurantId: "restaurant-1",
      customerId: "customer-1",
      start: "2026-11-14T19:00:00Z",
      end: "2026-11-14T21:00:00Z",
      numberOfGuests: 4,
      tableNumber: 2,
    };
    expect(notifier.toUser).toHaveBeenCalledWith("customer-1", expected);
    expect(notifier.toRestaurant).toHaveBeenCalledWith("restaurant-1", expected);
  });

  it("does not push payment-success events (they carry no recipient)", async () => {
    vi.spyOn(logger, "info").mockImplementation(() => {});
    const notifier = fakeNotifier();

    await new NotificationController(notifier).handlePaymentSuccess(
      new PaymentSuccessNotification("payment-1", "order-1", 100, Currency.EUR)
    );

    expect(notifier.toUser).not.toHaveBeenCalled();
    expect(notifier.toRestaurant).not.toHaveBeenCalled();
  });

  it("never rejects when a push fails, and still tries the other recipient", async () => {
    vi.spyOn(logger, "info").mockImplementation(() => {});
    const errorSpy = vi.spyOn(logger, "error").mockImplementation(() => {});
    const notifier = fakeNotifier();
    notifier.toUser.mockRejectedValue(new Error("socket gone"));

    await expect(
      new NotificationController(notifier).handleOrderStatusChanged(
        new OrderStatusChangedNotification("order-1", "restaurant-1", "customer-1", "READY", true)
      )
    ).resolves.toBeUndefined();

    expect(notifier.toRestaurant).toHaveBeenCalledTimes(1);
    expect(errorSpy).toHaveBeenCalledTimes(1);
  });
});
