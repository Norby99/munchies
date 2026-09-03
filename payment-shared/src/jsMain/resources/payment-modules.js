// payment-modules.js
const generated = require("./munchies-payment-shared.js");
const _payment = generated.com.munchies.payment;
const _inbound = _payment.infrastructure.adapter.inbound;
const _outbound = _payment.infrastructure.adapter.outbound;
const _commons = generated.com.munchies.commons;

module.exports = {
  Currency: _payment.infrastructure.adapter.dto.Currency,
  PaymentMethod: _payment.infrastructure.adapter.dto.PaymentMethod,
  PaymentDetails: _payment.infrastructure.adapter.dto.PaymentDetails,
  PaymentStatus: _payment.infrastructure.adapter.dto.PaymentStatus,
  PaymentServiceConfig: _inbound.web.config.PaymentServiceConfig,
  PaymentAPI: _inbound.PaymentAPI,
  ProcessPaymentAPI: _inbound.JsProcessPaymentAPI,
  ProcessPaymentRequest: _inbound.request.ProcessPaymentRequest,
  processPaymentRequestFromJson:
    _inbound.request.processPaymentRequestFromJson,
  ProcessPaymentResponse: _outbound.response.ProcessPaymentResponse,
  processPaymentResponseFromJson:
    _outbound.response.processPaymentResponseFromJson,
  ProcessPaymentRequestValidator:
    _payment.infrastructure.adapter.validator.ProcessPaymentRequestValidator,
  InputValidator: _commons.domain.port.InputValidator,
  InputValidatorResult: _commons.domain.port.InputValidatorResult,
  ValidInput: _commons.domain.port.ValidInput,
  InvalidInput: _commons.domain.port.InvalidInput,
};
