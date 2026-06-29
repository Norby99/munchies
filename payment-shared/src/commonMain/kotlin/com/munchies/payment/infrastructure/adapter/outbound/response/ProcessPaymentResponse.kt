package com.munchies.payment.infrastructure.adapter.outbound.response

import com.munchies.payment.infrastructure.adapter.dto.Currency
import com.munchies.payment.infrastructure.adapter.dto.PaymentStatus
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
data class ProcessPaymentResponse(
  val paymentId: String,
  val status: PaymentStatus,
  val amount: Int,
  val currency: Currency,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun processPaymentResponseFromJson(json: String): ProcessPaymentResponse =
  Json.decodeFromString(json)
