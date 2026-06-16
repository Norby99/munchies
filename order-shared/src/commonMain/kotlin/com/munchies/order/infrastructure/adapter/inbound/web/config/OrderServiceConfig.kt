package com.munchies.order.infrastructure.adapter.inbound.web.config

import kotlin.js.JsExport

@JsExport
object OrderServiceConfig {
  const val SERVICE_NAME = "order-service"
  const val SERVICE_PATH = "/orders/"

  const val SERVICE_PORT = 8080
}
