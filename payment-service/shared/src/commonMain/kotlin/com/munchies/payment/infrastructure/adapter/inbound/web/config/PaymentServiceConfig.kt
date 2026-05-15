package com.munchies.payment.infrastructure.adapter.inbound.web.config

import kotlin.js.JsExport

@JsExport
object PaymentServiceConfig {
  const val SERVICE_PATH = "/payment/"
  const val SERVICE_PORT = 8080
}
