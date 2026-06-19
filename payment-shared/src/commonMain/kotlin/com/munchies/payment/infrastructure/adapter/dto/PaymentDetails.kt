package com.munchies.payment.infrastructure.adapter.dto

import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class PaymentDetails(
  val amount: Int,
  val method: PaymentMethod,
  val currency: Currency,
)
