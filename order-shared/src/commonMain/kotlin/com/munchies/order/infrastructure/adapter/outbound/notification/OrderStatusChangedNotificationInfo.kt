package com.munchies.order.infrastructure.adapter.outbound.notification

import kotlin.js.JsExport

@JsExport
object OrderStatusChangedNotificationInfo {
  const val ORDER_ID_KEY = "order_id_key"
  const val RESTAURANT_ID_KEY = "restaurant_id_key"
  const val CUSTOMER_ID_KEY = "customer_id_key"
  const val STATUS_KEY = "status_key"
  const val ORDER_STATUS_CHANGED_TOPIC = "order_status_changed_topic"
  const val ORDER_STATUS_CHANGED_GROUP_ID = "order_status_changed_group_id"
}
