package com.munchies.payment.infrastructure.adapter.inbound

import com.munchies.commons.domain.port.AuthRole
import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.commons.infrastructure.adapter.HttpMethod
import com.munchies.commons.infrastructure.adapter.SimpleAPI
import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.commons.infrastructure.adapter.errorResponseFromJson
import com.munchies.payment.infrastructure.adapter.inbound.request.ProcessPaymentRequest
import com.munchies.payment.infrastructure.adapter.inbound.request.processPaymentRequestFromJson
import com.munchies.payment.infrastructure.adapter.inbound.web.config.PaymentServiceConfig
import com.munchies.payment.infrastructure.adapter.outbound.response.ProcessPaymentResponse
import com.munchies.payment.infrastructure.adapter.outbound.response.processPaymentResponseFromJson
import kotlin.js.JsExport
import kotlin.js.Promise

@JsExport
abstract class JsProcessPaymentAPI<E : WebResponse<Any>> :
  PaymentAPI.ProcessPaymentAPI<Promise<E>>,
  SimpleAPI<ProcessPaymentRequest, ProcessPaymentResponse>() {
  override fun getPath(): String =
    PaymentServiceConfig.SERVICE_PATH + PaymentServiceConfig.PROCESS_PAYMENT_PATH

  override fun getPort(): Int = PaymentServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.POST
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  abstract override fun processPayment(request: ProcessPaymentRequest): Promise<E>

  override fun parseRequest(json: String): ProcessPaymentRequest =
    processPaymentRequestFromJson(json)

  override fun parseResponse(json: String): ProcessPaymentResponse =
    processPaymentResponseFromJson(json)

  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
}
