package com.munchies.order.infrastructure.adapter.outbound.notification

import com.munchies.commons.domain.port.Notification
import com.munchies.commons.infrastructure.adapter.wireJson
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
data class OrderStatusChangedNotification(
  val order_id_key: String,
  val restaurant_id_key: String,
  val customer_id_key: String,
  val status_key: String,
) : Notification {
  fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun orderStatusChangedNotificationFromJson(json: String): OrderStatusChangedNotification =
  Json.decodeFromString(json)
