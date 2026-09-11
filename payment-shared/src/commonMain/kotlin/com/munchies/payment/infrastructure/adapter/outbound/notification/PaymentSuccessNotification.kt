package com.munchies.payment.infrastructure.adapter.outbound.notification

import com.munchies.commons.domain.port.Notification
import com.munchies.commons.infrastructure.adapter.wireJson
import com.munchies.payment.infrastructure.adapter.dto.Currency
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
data class PaymentSuccessNotification(
  val payment_id_key: String,
  val order_id_key: String,
  val amount_key: Int,
  val currency_key: Currency,
) : Notification {
  fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun paymentSuccessNotificationFromJson(json: String): PaymentSuccessNotification =
  Json.decodeFromString(json)
