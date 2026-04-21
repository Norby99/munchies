import type * as CommonsModule from "munchies-commons";
import type * as SharedModule from "munchies-payment-service-shared";

const commonsModule = require("munchies-commons") as typeof CommonsModule;
const sharedModule =
  require("munchies-payment-service-shared") as typeof SharedModule;

const _commons = commonsModule.com.munchies.commons;
const _inbound =
  sharedModule.com.munchies.payment.infrastructure.adapter.inbound;
const _adapter = sharedModule.com.munchies.payment.infrastructure.adapter;

// Values
export const newUUIDEntityId = _commons.newUUIDEntityId;
export const getIdFromEntityId = _commons.getIdFromEntityId;
export const newId = () => getIdFromEntityId(newUUIDEntityId(null));
export const PaymentResponse = _inbound.response.ProcessPaymentResponse;
export const PaymentRequest = _inbound.request.ProcessPaymentRequest;
export const PaymentStatus = _adapter.dto.PaymentStatus;
export const Currency = _adapter.dto.Currency;

const _PaymentAPI = _inbound.PaymentAPI;

// Types
export type UUIDEntityId = InstanceType<typeof _commons.UUIDEntityId>;
export type PaymentResponse = InstanceType<typeof PaymentResponse>;
export type PaymentRequest = InstanceType<typeof PaymentRequest>;
export type PaymentAPI = InstanceType<typeof _PaymentAPI>;
