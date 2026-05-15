package com.munchies.payment.infrastructure.adapter.inbound.response

import com.munchies.payment.infrastructure.adapter.dto.Currency
import com.munchies.payment.infrastructure.adapter.dto.PaymentStatus
import kotlin.js.JsExport

@JsExport
data class ProcessPaymentResponse(
  val paymentId: String,
  val status: PaymentStatus,
  val amount: Int,
  val currency: Currency,
)
