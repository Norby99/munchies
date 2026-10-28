package com.munchies.order.infrastructure.adapter.outbound.mongo.repository

import com.mongodb.client.MongoCollection
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.event.OrderPaid
import com.munchies.order.domain.model.view.OrderView
import com.munchies.order.domain.port.ConcurrentOrderModificationException
import com.munchies.order.fixtures.asHistory
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderEventDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderViewDocument
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.micronaut.context.ApplicationContext
import io.micronaut.inject.qualifiers.Qualifiers
import org.bson.Document
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.mongodb.MongoDBContainer

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MongoOrderEventStoreIntegrationTest {

  companion object {
    @Container
    @JvmStatic
    private val mongo = MongoDBContainer("mongo:7.0")
  }

  private lateinit var context: ApplicationContext
  private lateinit var eventStore: MongoOrderEventStore
  private lateinit var views: MongoOrderViewRepository

  @BeforeAll
  fun setup() {
    context = ApplicationContext.run(
      mapOf("mongodb.uri" to "${mongo.connectionString}/order-service"),
      "prod",
    )
    eventStore = context.getBean(MongoOrderEventStore::class.java)
    views = context.getBean(MongoOrderViewRepository::class.java)
  }

  /**
   * Clean up both collections after each test, keeping their indexes.
   */
  @AfterEach
  fun cleanup() {
    listOf(OrderEventDocument.COLLECTION, OrderViewDocument.COLLECTION).forEach {
      context.getBean(MongoCollection::class.java, Qualifiers.byName(it))
        .deleteMany(Document())
    }
  }

  @AfterAll
  fun tearDown() {
    context.close()
  }

  @Test
  fun `load returns the appended events in sequence order`() {
    val history = createDeliveryOrder(status = OrderStatus.READY).asHistory()

    eventStore.append(defaultOrderId, 0, history.take(2))
    eventStore.append(defaultOrderId, 2, history.drop(2))

    eventStore.load(defaultOrderId).map { it::class } shouldBe history.map { it::class }
  }

  @Test
  fun `load returns an empty stream when the order does not exist`() {
    eventStore.load(OrderId("non-existent-id")).shouldBeEmpty()
  }

  @Test
  fun `append rejects a stale expected version without writing anything`() {
    val history = createDeliveryOrder().asHistory()
    eventStore.append(defaultOrderId, 0, history)

    assertThrows<ConcurrentOrderModificationException> {
      eventStore.append(defaultOrderId, 0, listOf(OrderPaid(defaultOrderId)))
    }

    eventStore.load(defaultOrderId).size shouldBe history.size
  }

  @Test
  fun `append feeds the read model projection`() {
    val history = createDeliveryOrder(status = OrderStatus.PREPARING).asHistory()

    eventStore.append(defaultOrderId, 0, history)
    eventStore.append(defaultOrderId, history.size.toLong(), listOf(OrderPaid(defaultOrderId)))

    val view = views.findById(defaultOrderId)
    view shouldBe OrderView.fromHistory(eventStore.load(defaultOrderId))
    view?.payed shouldBe true
  }
}
