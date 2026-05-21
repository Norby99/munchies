package com.munchies.payment.infrastructure.adapter.dto

import kotlin.js.JsExport

@JsExport
data class PaymentDetails(
  val amount: Int,
  val method: PaymentMethod,
  val currency: Currency,
)
