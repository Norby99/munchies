import type * as CommonsModule from "munchies-commons";
import type * as SharedModule from "munchies-payment-service-shared";

const commonsModule = require("munchies-commons") as typeof CommonsModule;
const sharedModule =
  require("munchies-payment-service-shared") as typeof SharedModule;

const commons = commonsModule.com.munchies.commons;
const paymentShared =
  sharedModule.com.munchies.payment.infrastructure.adapter.dto;

console.log("Starting payment service...");

console.log(commons);
console.log(paymentShared);

const paymentId = commons.newUUIDEntityId(null);
const command = new paymentShared.PaymentCommand(
  "order-1",
  2590,
  paymentShared.PaymentMethod.CARD
);
const result = new paymentShared.PaymentResult(
  commons.getIdFromEntityId(paymentId),
  true
);

console.log({ command, result });
