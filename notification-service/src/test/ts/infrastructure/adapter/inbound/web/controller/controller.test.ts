import { afterEach, describe, expect, it, vi } from "vitest";
import { NotificationController } from "@main/infrastructure/adapter/inbound/web/controller/controller";
import {
  PaymentSuccessNotification,
  UserEmailConfirmationNotification,
} from "@main/domain/external-modules";
import { Currency } from "munchies-payment-service-shared/kotlin/payment-modules";

describe("NotificationController", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("logs a user email-confirmation event instead of sending a notification", () => {
    const logSpy = vi.spyOn(console, "log").mockImplementation(() => {});
    const controller = new NotificationController();
    const event = new UserEmailConfirmationNotification("user-1", "otk-123");

    controller.handleUserEmailConfirmation(event);

    expect(logSpy).toHaveBeenCalledTimes(1);
    const [loggedMessage] = logSpy.mock.calls[0];
    expect(loggedMessage).toContain("UserEmailConfirmationNotification");
    expect(loggedMessage).toContain("user-1");
  });

  it("logs a payment-success event instead of sending a notification", () => {
    const logSpy = vi.spyOn(console, "log").mockImplementation(() => {});
    const controller = new NotificationController();
    const event = new PaymentSuccessNotification(
      "payment-1",
      "order-1",
      100,
      Currency.EUR
    );

    controller.handlePaymentSuccess(event);

    expect(logSpy).toHaveBeenCalledTimes(1);
    const [loggedMessage] = logSpy.mock.calls[0];
    expect(loggedMessage).toContain("PaymentSuccessNotification");
    expect(loggedMessage).toContain("payment-1");
  });
});
