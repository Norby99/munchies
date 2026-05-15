import { describe, expect, it } from "vitest";
import { PaymentController } from "@main/infrastructure/adapter/inbound/web/controller/controller";

describe("PaymentController", () => {
  it("creates a payment", async () => {
    const controller = new PaymentController();

    5 == 5;
  });
});
