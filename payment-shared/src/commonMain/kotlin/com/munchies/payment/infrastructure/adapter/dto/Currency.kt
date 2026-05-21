package com.munchies.payment.infrastructure.adapter.dto

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
enum class Currency {
  USD,
  EUR,
  GBP,
  JPY,
  AUD,
  CAD,
  CHF,
  CNY,
  SEK,
  NZD,
}
