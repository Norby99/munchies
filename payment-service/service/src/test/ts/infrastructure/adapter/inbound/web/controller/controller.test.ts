import { describe, expect, it } from "vitest";
import { PaymentController } from "@main/infrastructure/adapter/inbound/web/controller/controller";

describe("PaymentController", () => {
  it("creates a payment", async () => {
    const controller = new PaymentController();

    await expect(
      controller.createPayment({
        orderId: "order-1",
        amountCents: 2590,
      })
    ).resolves.toEqual({
      paymentId: "p-123",
      accepted: true,
    });
  });
});
