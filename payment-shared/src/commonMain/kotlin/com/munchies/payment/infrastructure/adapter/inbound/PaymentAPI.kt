package com.munchies.payment.infrastructure.adapter.inbound

import com.munchies.payment.infrastructure.adapter.inbound.request.ProcessPaymentRequest
import kotlin.js.JsExport

@JsExport
object PaymentAPI {
  interface ProcessPaymentAPI<Response> {
    fun processPayment(request: ProcessPaymentRequest): Response
  }
}
