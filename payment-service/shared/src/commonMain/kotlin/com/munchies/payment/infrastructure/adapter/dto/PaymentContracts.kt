package com.munchies.payment.infrastructure.adapter.dto

import kotlin.js.JsExport

@JsExport
enum class PaymentMethod {
  CARD,
  PIX,
  CASH,
}

@JsExport
data class PaymentCommand(
  val orderId: String,
  val amountInCents: Int,
  val method: PaymentMethod,
)

@JsExport
data class PaymentResult(
  val paymentId: String,
  val accepted: Boolean,
)
