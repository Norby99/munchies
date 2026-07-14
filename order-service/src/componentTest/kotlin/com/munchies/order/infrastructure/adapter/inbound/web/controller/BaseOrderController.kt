package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.domain.model.OrderId
import com.munchies.order.infrastructure.adapter.dto.*
import com.munchies.order.infrastructure.adapter.inbound.request.*
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.serde.ObjectMapper
import io.micronaut.serde.annotation.SerdeImport
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.MongoDBContainer

@SerdeImport(OrderId::class)
@SerdeImport(OrderDto::class)
@SerdeImport(OrderDto.Delivery::class)
@SerdeImport(OrderDto.Takeaway::class)
@SerdeImport(OrderDto.DineIn::class)
@SerdeImport(OrderItemDto::class)
@SerdeImport(OrderType::class)
@SerdeImport(PlaceOrderRequest::class)
@SerdeImport(GetOrderDetailsRequest::class)
@SerdeImport(AdvanceOrderStatusRequest::class)
@SerdeImport(DiscardOrderRequest::class)
@SerdeImport(UpdateOrderItemsRequest::class)
@SerdeImport(UpdateDeliveryOrderRequest::class)
@SerdeImport(UpdateTakeawayOrderRequest::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseOrderController : TestPropertyProvider {

  companion object {
    private val mongo: MongoDBContainer by lazy {
      MongoDBContainer("mongo:7.0").apply { start() }
    }
  }

  override fun getProperties(): MutableMap<String, String> = mutableMapOf(
    "mongodb.uri" to "${mongo.connectionString}/order-service",
    "mongodb.package-names[0]" to
      "com.munchies.order.infrastructure.adapter.outbound.mongo.document",
  )

  @Inject
  @field:Client("/")
  lateinit var client: HttpClient

  @Inject
  lateinit var mapper: ObjectMapper

  @Inject
  lateinit var embeddedServer: EmbeddedServer

  fun baseUrl(): String =
    "http://localhost:${embeddedServer.port}${OrderServiceConfig.SERVICE_PATH}"

  fun httpPost(request: Any, endPoint: String): HttpResponse<String> = client.toBlocking().exchange(
    HttpRequest.POST(
      "${baseUrl()}$endPoint",
      request,
    ),
    String::class.java,
  )

  fun httpPatch(request: Any, endPoint: String): HttpResponse<String> =
    client.toBlocking().exchange(
      HttpRequest.PATCH(
        "${baseUrl()}$endPoint",
        request,
      ),
      String::class.java,
    )

  fun httpGet(endPoint: String): HttpResponse<OrderDto.Takeaway> = client.toBlocking().exchange(
    HttpRequest.GET<Any>("${baseUrl()}$endPoint"),
    OrderDto.Takeaway::class.java,
  )
}
