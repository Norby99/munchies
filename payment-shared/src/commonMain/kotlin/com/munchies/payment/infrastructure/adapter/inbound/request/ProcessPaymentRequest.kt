package com.munchies.payment.infrastructure.adapter.inbound.request

import com.munchies.commons.infrastructure.adapter.wireJson
import com.munchies.payment.infrastructure.adapter.dto.PaymentDetails
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Request to pay an order.
 *
 * @property orderId The order to pay.
 * @property customerId The paying customer. Clients leave it empty: the gateway fills it from the
 * authentication token ([addId]), and payment-service only charges orders of that customer.
 * @property paymentDetails How the customer pays.
 */
@JsExport
@Serializable
data class ProcessPaymentRequest(
  val orderId: String,
  val customerId: String = "",
  val paymentDetails: PaymentDetails,
) {
  fun toJson(): String = wireJson.encodeToString(this)

  /**
   * Same contract as `AuthenticatedRequest.addId`, without implementing the interface: in the JS
   * typings that interface becomes a `unique symbol` marker, which payment-service's OpenAPI
   * generator (tsoa) cannot parse.
   */
  fun addId(userId: String): ProcessPaymentRequest = copy(customerId = userId)
}

@JsExport
fun processPaymentRequestFromJson(json: String): ProcessPaymentRequest = Json.decodeFromString(json)
