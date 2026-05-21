package com.munchies.payment.infrastructure.adapter.inbound

import com.munchies.payment.infrastructure.adapter.inbound.request.ProcessPaymentRequest
import com.munchies.payment.infrastructure.adapter.inbound.response.ProcessPaymentResponse
import kotlin.js.JsExport

@JsExport
interface PaymentInterface {
  fun processPayment(request: ProcessPaymentRequest): ProcessPaymentResponse
}

@JsExport
abstract class PaymentAPI : PaymentInterface
