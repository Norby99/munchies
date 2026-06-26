// payment-modules.js
const generated = require("./munchies-payment-shared.js");
const _payment = generated.com.munchies.payment;

module.exports = {
  Currency: _payment.infrastructure.adapter.dto.Currency,
  PaymentMethod: _payment.infrastructure.adapter.dto.PaymentMethod,
  PaymentDetails: _payment.infrastructure.adapter.dto.PaymentDetails,
  PaymentStatus: _payment.infrastructure.adapter.dto.PaymentStatus,
  PaymentAPI: _payment.infrastructure.adapter.inbound.PaymentAPI,
  ProcessPaymentRequest:
    _payment.infrastructure.adapter.inbound.request.ProcessPaymentRequest,
  ProcessPaymentResponse:
    _payment.infrastructure.adapter.outbound.response.ProcessPaymentResponse,
  ProcessRequestValidator:
    _payment.infrastructure.adapter.validator.ProcessRequestValidator,
  processPaymentRequestFromJson:
    _payment.infrastructure.adapter.inbound.request
      .processPaymentRequestFromJson,
  processPaymentResponseFromJson:
    _payment.infrastructure.adapter.outbound.response
      .processPaymentResponseFromJson,
  PaymentServiceConfig:
    _payment.infrastructure.adapter.inbound.web.config.PaymentServiceConfig,
};
