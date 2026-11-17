package com.munchies.payment.infrastructure.adapter.validator

import com.munchies.commons.domain.port.InputValidator
import com.munchies.commons.domain.port.InputValidatorResult
import com.munchies.commons.domain.port.InvalidInput
import com.munchies.commons.domain.port.ValidInput
import com.munchies.payment.infrastructure.adapter.inbound.request.ProcessPaymentRequest
import kotlin.js.JsExport

@JsExport
class ProcessPaymentRequestValidator : InputValidator<ProcessPaymentRequest>() {
  override fun validate(input: ProcessPaymentRequest): InputValidatorResult {
    if (input.orderId.isEmpty()) return InvalidInput("Missing Order Id")
    // Filled by the gateway from the auth token: only that customer's orders can be paid.
    if (input.customerId.isEmpty()) return InvalidInput("Missing Customer Id")
    return ValidInput
  }
}
