package com.munchies.payment.infrastructure.adapter.inbound.web.config

import kotlin.js.JsExport

@JsExport
object PaymentServiceConfig {
  const val SERVICE_NAME = "payment-service"
  const val SERVICE_PATH = "/payments/"
  const val PROCESS_PAYMENT_PATH = ""
  const val SERVICE_PORT = 8080
}
