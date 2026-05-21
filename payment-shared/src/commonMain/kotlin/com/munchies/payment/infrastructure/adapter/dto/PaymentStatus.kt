package com.munchies.payment.infrastructure.adapter.dto

import kotlin.js.JsExport

@JsExport
enum class PaymentStatus {
  PENDING,
  COMPLETED,
  FAILED,
  CANCELLED,
}
