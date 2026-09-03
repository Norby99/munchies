// payment-modules.d.ts
import type { com } from "./munchies-payment-shared";

export type Currency = com.munchies.payment.infrastructure.adapter.dto.Currency;
export declare const Currency: typeof com.munchies.payment.infrastructure.adapter.dto.Currency;

export type PaymentMethod =
  com.munchies.payment.infrastructure.adapter.dto.PaymentMethod;
export declare const PaymentMethod: typeof com.munchies.payment.infrastructure.adapter.dto.PaymentMethod;

export type PaymentDetails =
  com.munchies.payment.infrastructure.adapter.dto.PaymentDetails;
export declare const PaymentDetails: typeof com.munchies.payment.infrastructure.adapter.dto.PaymentDetails;

export type PaymentStatus =
  com.munchies.payment.infrastructure.adapter.dto.PaymentStatus;
export declare const PaymentStatus: typeof com.munchies.payment.infrastructure.adapter.dto.PaymentStatus;

export declare const PaymentServiceConfig: typeof com.munchies.payment.infrastructure.adapter.inbound.web.config.PaymentServiceConfig;

export type PaymentAPI =
  com.munchies.payment.infrastructure.adapter.inbound.PaymentAPI;
export declare const PaymentAPI: typeof com.munchies.payment.infrastructure.adapter.inbound.PaymentAPI;

export type ProcessPaymentAPI =
  com.munchies.payment.infrastructure.adapter.inbound.JsProcessPaymentAPI;
export declare const ProcessPaymentAPI: typeof com.munchies.payment.infrastructure.adapter.inbound.JsProcessPaymentAPI;

export type ProcessPaymentRequest =
  com.munchies.payment.infrastructure.adapter.inbound.request.ProcessPaymentRequest;
export declare const ProcessPaymentRequest: typeof com.munchies.payment.infrastructure.adapter.inbound.request.ProcessPaymentRequest;

export type ProcessPaymentResponse =
  com.munchies.payment.infrastructure.adapter.outbound.response.ProcessPaymentResponse;
export declare const ProcessPaymentResponse: typeof com.munchies.payment.infrastructure.adapter.outbound.response.ProcessPaymentResponse;

export type ProcessPaymentRequestValidator =
  com.munchies.payment.infrastructure.adapter.validator.ProcessPaymentRequestValidator;
export declare const ProcessPaymentRequestValidator: typeof com.munchies.payment.infrastructure.adapter.validator.ProcessPaymentRequestValidator;

export type InputValidatorResult =
  com.munchies.commons.domain.port.InputValidatorResult;
export declare const InputValidatorResult: typeof com.munchies.commons.domain.port.InputValidatorResult;

export type ValidInput =
  typeof com.munchies.commons.domain.port.ValidInput;
export declare const ValidInput: typeof com.munchies.commons.domain.port.ValidInput;

export type InvalidInput =
  com.munchies.commons.domain.port.InvalidInput;
export declare const InvalidInput: typeof com.munchies.commons.domain.port.InvalidInput;

export declare const processPaymentRequestFromJson: typeof com.munchies.payment.infrastructure.adapter.inbound.request.processPaymentRequestFromJson;
export declare const processPaymentResponseFromJson: typeof com.munchies.payment.infrastructure.adapter.outbound.response.processPaymentResponseFromJson;
