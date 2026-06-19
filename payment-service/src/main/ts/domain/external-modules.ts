import type * as CommonsModule from "munchies-commons";
import type * as SharedModule from "munchies-payment-service-shared";

const commonsModule = require("munchies-commons") as typeof CommonsModule;
const sharedModule =
  require("munchies-payment-service-shared") as typeof SharedModule;

const _commons = commonsModule.com.munchies.commons;
const _inbound =
  sharedModule.com.munchies.payment.infrastructure.adapter.inbound;
const _adapter = sharedModule.com.munchies.payment.infrastructure.adapter;

export const PaymentResponse = _inbound.response.ProcessPaymentResponse;
export const PaymentRequest = _inbound.request.ProcessPaymentRequest;
export const PaymentStatus = _adapter.dto.PaymentStatus;
export const Currency = _adapter.dto.Currency;
export const PaymentDetails = _adapter.dto.PaymentDetails;
export const PaymentMethod = _adapter.dto.PaymentMethod;

const _PaymentAPI = _inbound.PaymentAPI;
export const PaymentAPI = _PaymentAPI;

export type PaymentMethod =
  (typeof _adapter.dto.PaymentMethod)[keyof typeof _adapter.dto.PaymentMethod];
export type PaymentStatus =
  (typeof _adapter.dto.PaymentStatus)[keyof typeof _adapter.dto.PaymentStatus];
export type Currency =
  (typeof _adapter.dto.Currency)[keyof typeof _adapter.dto.Currency];

export type PaymentDetails = InstanceType<typeof PaymentDetails>;
export type PaymentResponse = InstanceType<typeof PaymentResponse>;
export type PaymentRequest = InstanceType<typeof PaymentRequest>;
export type PaymentAPI = InstanceType<typeof _PaymentAPI>;
