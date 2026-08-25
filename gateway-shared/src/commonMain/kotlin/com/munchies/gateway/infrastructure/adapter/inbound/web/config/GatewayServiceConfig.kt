package com.munchies.gateway.infrastructure.adapter.inbound.web.config

import kotlin.js.JsExport

@JsExport
object GatewayServiceConfig {
  const val SERVICE_NAME = "gateway-service"
  const val SERVICE_PATH = "/users/"
  const val LOGOUT_USER_PATH = "logout/"

  const val SERVICE_PORT = 8080
}
