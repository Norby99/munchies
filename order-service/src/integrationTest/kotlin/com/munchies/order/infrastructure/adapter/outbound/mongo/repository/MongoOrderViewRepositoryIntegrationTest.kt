package com.munchies.order.infrastructure.adapter.outbound.mongo.repository

import com.mongodb.client.MongoCollection
import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.fixtures.asView
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createTakeawayOrder
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderViewDocument
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.context.ApplicationContext
import io.micronaut.inject.qualifiers.Qualifiers
import org.bson.Document
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.mongodb.MongoDBContainer

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MongoOrderViewRepositoryIntegrationTest {

  companion object {
    @Container
    @JvmStatic
    private val mongo = MongoDBContainer("mongo:7.0")
  }

  private lateinit var context: ApplicationContext
  private lateinit var views: MongoOrderViewRepository

  private val delivery = createDeliveryOrder().asView()
  private val takeaway = createTakeawayOrder(status = OrderStatus.READY).asView()
  private val otherRestaurant = createDeliveryOrder().copy(
    id = OrderId("o-other"),
    restaurantId = RestaurantId("r-other"),
    customerId = CustomerId("c-other"),
  ).asView()

  @BeforeAll
  fun setup() {
    context = ApplicationContext.run(
      mapOf("mongodb.uri" to "${mongo.connectionString}/order-service"),
      "prod",
    )
    views = context.getBean(MongoOrderViewRepository::class.java)
  }

  @AfterEach
  fun cleanup() {
    context.getBean(MongoCollection::class.java, Qualifiers.byName(OrderViewDocument.COLLECTION))
      .deleteMany(Document())
  }

  @AfterAll
  fun tearDown() {
    context.close()
  }

  @Test
  fun `saves and retrieves a view by order id`() {
    views.save(delivery)

    views.findById(delivery.orderId) shouldBe delivery
  }

  @Test
  fun `findById returns null when the view does not exist`() {
    views.findById(OrderId("non-existent-id")).shouldBeNull()
  }

  @Test
  fun `save replaces the existing view of the same order`() {
    views.save(delivery)
    val updated = delivery.copy(status = OrderStatus.PREPARING, version = delivery.version + 1)

    views.save(updated)

    views.findById(delivery.orderId) shouldBe updated
    views.findBy(null, null, null).size shouldBe 1
  }

  @Test
  fun `findBy combines the given filters and ignores the null ones`() {
    // delivery and takeaway share id o-1 and o-t-1, restaurant r-1 and customer c-1.
    listOf(delivery, takeaway, otherRestaurant).forEach(views::save)

    views.findBy(null, null, null) shouldContainExactlyInAnyOrder
      listOf(delivery, takeaway, otherRestaurant)
    views.findBy(delivery.restaurantId, null, null) shouldContainExactlyInAnyOrder
      listOf(delivery, takeaway)
    views.findBy(delivery.restaurantId, null, OrderStatus.READY) shouldContainExactly
      listOf(takeaway)
    views.findBy(null, otherRestaurant.customerId, OrderStatus.PENDING) shouldContainExactly
      listOf(otherRestaurant)
    views.findBy(delivery.restaurantId, otherRestaurant.customerId, null) shouldBe emptyList()
  }
}
