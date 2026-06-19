package com.munchies.payment.infrastructure.adapter.inbound.request

import com.munchies.payment.infrastructure.adapter.dto.PaymentDetails
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
data class ProcessPaymentRequest(
  val orderId: String,
  val paymentDetails: PaymentDetails,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun processPaymentRequestFromJson(json: String): ProcessPaymentRequest = Json.decodeFromString(json)
