package com.munchies.payment.infrastructure.adapter.inbound.response

import kotlin.js.JsExport

@JsExport
enum class PaymentStatus {
  PENDING,
  COMPLETED,
  FAILED,
  CANCELLED,
}
