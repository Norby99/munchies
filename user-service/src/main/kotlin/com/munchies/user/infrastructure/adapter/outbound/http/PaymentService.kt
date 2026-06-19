package com.munchies.user.infrastructure.adapter.outbound.http

import com.munchies.payment.infrastructure.adapter.dto.Currency
import com.munchies.payment.infrastructure.adapter.dto.PaymentStatus
import com.munchies.payment.infrastructure.adapter.inbound.PaymentAPI
import com.munchies.payment.infrastructure.adapter.inbound.request.ProcessPaymentRequest
import com.munchies.payment.infrastructure.adapter.inbound.response.ProcessPaymentResponse
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.serde.ObjectMapper
import io.micronaut.serde.annotation.SerdeImport
import jakarta.inject.Singleton

@SerdeImport(ProcessPaymentRequest::class)
@SerdeImport(ProcessPaymentResponse::class)
@SerdeImport(PaymentStatus::class)
@SerdeImport(Currency::class)
@Singleton
class PaymentService(
  @Client("\${micronaut.http.services.payment-service.url}")
  private val httpClient: HttpClient,
  private val objectMapper: ObjectMapper,
) : PaymentAPI() {
  override fun processPayment(request: ProcessPaymentRequest): ProcessPaymentResponse {
    val req = io.micronaut.http.HttpRequest.POST("/payments", request.toJson())
    val response = httpClient.toBlocking().exchange(req, String::class.java)
    return objectMapper
      .readValue(response.body(), ProcessPaymentResponse::class.java)
  }
}
