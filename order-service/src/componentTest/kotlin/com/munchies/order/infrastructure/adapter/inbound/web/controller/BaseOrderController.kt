package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.mongodb.client.MongoCollection
import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.order.domain.factory.OrderFactory
import com.munchies.order.domain.model.Order
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.port.OrderEventStore
import com.munchies.order.domain.port.OrderViewRepository
import com.munchies.order.fixtures.asHistory
import com.munchies.order.infrastructure.adapter.dto.*
import com.munchies.order.infrastructure.adapter.inbound.request.*
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderEventDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderViewDocument
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.serde.ObjectMapper
import io.micronaut.serde.annotation.SerdeImport
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import jakarta.inject.Named
import org.bson.Document
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.TestInstance
import org.testcontainers.mongodb.MongoDBContainer

@SerdeImport(OrderId::class)
@SerdeImport(OrderDto::class)
@SerdeImport(OrderItemDto::class)
@SerdeImport(OrderType::class)
@SerdeImport(PlaceOrderRequest::class)
@SerdeImport(AdvanceOrderStatusRequest::class)
@SerdeImport(UpdateOrderItemsRequest::class)
@SerdeImport(UpdateDeliveryOrderRequest::class)
@SerdeImport(UpdateTakeawayOrderRequest::class)
@SerdeImport(ErrorResponse::class)
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

  @Inject
  lateinit var eventStore: OrderEventStore

  @Inject
  lateinit var orderViews: OrderViewRepository

  @Inject
  @field:Named(OrderEventDocument.COLLECTION)
  lateinit var eventCollection: MongoCollection<Document>

  @Inject
  @field:Named(OrderViewDocument.COLLECTION)
  lateinit var viewCollection: MongoCollection<Document>

  /**
   * Test-side access to the command side: seeds an order by appending an event stream that
   * reproduces it (which also feeds the read model projection), and reads the current state
   * of an order by replaying its stream.
   */
  val orderRepository: OrderStreams by lazy { OrderStreams() }

  inner class OrderStreams {
    fun save(order: Order) = eventStore.append(order.id, 0, order.asHistory())

    fun findById(id: OrderId): Order? = OrderFactory.fromHistory(eventStore.load(id))
  }

  @AfterEach
  fun cleanupMongo() {
    eventCollection.deleteMany(Document())
    viewCollection.deleteMany(Document())
  }

  val httpCalls: HttpCalls by lazy { HttpCalls(baseUrl(), client) }

  inline fun <reified T> HttpResponse<*>.bd() = this.getBody(T::class.java).get()

  /**
   * @return the base URL for the order service, including the embedded server
   * port and service path.
   */
  private fun baseUrl(): String =
    "http://localhost:${embeddedServer.port}${OrderServiceConfig.SERVICE_PATH}"
}
