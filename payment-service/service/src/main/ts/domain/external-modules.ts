import type * as CommonsModule from "munchies-commons";
import type * as SharedModule from "munchies-payment-service-shared";

const commonsModule = require("munchies-commons") as typeof CommonsModule;
const sharedModule =
  require("munchies-payment-service-shared") as typeof SharedModule;

const _commons = commonsModule.com.munchies.commons;
const _shared =
  sharedModule.com.munchies.payment.infrastructure.adapter.inbound;

// Values
export const newUUIDEntityId = _commons.newUUIDEntityId;
export const PaymentResponse = _shared.response.ProcessPaymentResponse;
export const PaymentRequest = _shared.request.ProcessPaymentRequest;
export const PaymentStatus = _shared.response.PaymentStatus;
export const Currency = _shared.request.Currency;

const _PaymentAPI = _shared.PaymentAPI;

// Types
export type PaymentResponse = InstanceType<typeof PaymentResponse>;
export type PaymentRequest = InstanceType<typeof PaymentRequest>;
export type PaymentAPI = InstanceType<typeof _PaymentAPI>;
