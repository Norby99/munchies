package com.munchies.order.infrastructure.adapter.outbound.notification.orderstatuschanged

import com.munchies.commons.domain.port.Notification
import com.munchies.commons.infrastructure.adapter.wireJson
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Published by order-service whenever an order changes in a way its customer or its restaurant
 * can see: placed, paid, advanced to its next status, or cancelled. Event-carried state transfer:
 * it carries the order's current status and whether it is paid, which together tell what
 * happened (e.g. a PENDING order not paid yet was just placed, a PENDING paid one was just paid).
 *
 * @property payed_key Whether the order is paid. Defaults to false so that events published
 * before the field existed still decode.
 */
@JsExport
@Serializable
data class OrderStatusChangedNotification(
  val order_id_key: String,
  val restaurant_id_key: String,
  val customer_id_key: String,
  val status_key: String,
  val payed_key: Boolean = false,
) : Notification {
  fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun orderStatusChangedNotificationFromJson(json: String): OrderStatusChangedNotification =
  Json.decodeFromString(json)
