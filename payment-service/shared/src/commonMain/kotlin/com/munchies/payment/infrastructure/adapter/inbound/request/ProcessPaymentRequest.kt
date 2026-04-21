package com.munchies.payment.infrastructure.adapter.inbound.request

import com.munchies.commons.UUIDEntityId
import kotlin.js.JsExport

@JsExport
data class ProcessPaymentRequest(
  val orderId: UUIDEntityId,
  val paymentDetails: PaymentDetails,
)
