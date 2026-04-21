package com.munchies.payment.infrastructure.adapter.inbound.request

import com.munchies.payment.infrastructure.adapter.dto.PaymentDetails
import kotlin.js.JsExport

@JsExport
data class ProcessPaymentRequest(
  val orderId: String,
  val paymentDetails: PaymentDetails,
)
