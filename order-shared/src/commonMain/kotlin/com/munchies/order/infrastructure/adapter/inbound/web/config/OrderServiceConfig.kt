package com.munchies.order.infrastructure.adapter.inbound.web.config

import kotlin.js.JsExport

@JsExport
object OrderServiceConfig {
  const val SERVICE_NAME = "order-service"
  const val SERVICE_PATH = "/orders/"

  const val SERVICE_PORT = 8080

  const val GET_ORDER_PATH = "{id}"
  const val PLACE_ORDER_PATH = "place"
  const val ADVANCE_ORDER_STATUS_PATH = "advance"
  const val DISCARD_ORDER_PATH = "discard"
  const val UPDATE_ORDER_ITEMS_PATH = "items"
  const val UPDATE_DELIVERY_ORDER_INFO_PATH = "delivery"
  const val UPDATE_TAKEAWAY_ORDER_INFO_PATH = "takeaway"
}
