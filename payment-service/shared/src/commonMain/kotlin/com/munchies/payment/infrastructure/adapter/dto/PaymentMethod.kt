package com.munchies.payment.infrastructure.adapter.dto

import kotlin.js.JsExport

@JsExport
enum class PaymentMethod {
  CARD,
  CASH,
  MOBILE_PAYMENT,
  BANK_TRANSFER,
  OTHER,
}
