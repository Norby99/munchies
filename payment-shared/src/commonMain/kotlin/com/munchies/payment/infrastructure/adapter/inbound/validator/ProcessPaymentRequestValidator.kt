package com.munchies.payment.infrastructure.adapter.inbound.validator

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
    if (input.paymentDetails.amount < 0) return InvalidInput("Negative owed amount")
    return ValidInput
  }
}
