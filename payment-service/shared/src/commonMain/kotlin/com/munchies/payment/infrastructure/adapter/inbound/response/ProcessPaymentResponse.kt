package com.munchies.payment.infrastructure.adapter.inbound.response

import com.munchies.commons.UUIDEntityId
import com.munchies.payment.infrastructure.adapter.inbound.request.Currency
import kotlin.js.JsExport

@JsExport
data class ProcessPaymentResponse(
  val paymentId: UUIDEntityId,
  val status: PaymentStatus,
  val amount: Int,
  val currency: Currency,
)
