package com.munchies.order.infrastructure.adapter.outbound.mongo.repository

import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.fixtures.createDeliveryOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.context.ApplicationContext
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MongoOrderRepositoryIntegrationTest {

  companion object {
    @Container
    @JvmStatic
    private val mongo = MongoDBContainer("mongo:7.0")
  }

  private lateinit var context: ApplicationContext
  private lateinit var repository: MongoOrderRepository

  @BeforeAll
  fun setup() {
    context = ApplicationContext.run(
      mapOf(
        "mongodb.uri" to "${mongo.connectionString}/order-service",
        "mongodb.package-names" to listOf(
          "com.munchies.order.infrastructure.adapter.outbound.mongo.document",
        ),
      ),
      "prod",
    )
    repository = context.getBean(MongoOrderRepository::class.java)
  }

  /**
   * Clean up the database after each test to ensure isolation between tests.
   */
  @AfterEach
  fun cleanup() {
    context.getBean(MongoCrudOrderRepository::class.java).deleteAll()
  }

  @AfterAll
  fun tearDown() {
    context.close()
  }

  @Test
  fun `saves and retrieves an order by id`() {
    val order = createDeliveryOrder()

    repository.save(order)
    val found = repository.findById(order.id)

    found shouldBe order
  }

  @Test
  fun `findById returns null when the order does not exist`() {
    repository.findById(OrderId("non-existent-id")).shouldBeNull()
  }

  @Test
  fun `update persists changes to an existing order`() {
    val order = createDeliveryOrder()
    repository.save(order)

    val updated = order.copy(status = OrderStatus.COMPLETED)
    repository.update(updated)

    repository.findById(order.id) shouldBe updated
  }

  @Test
  fun `delete removes the order`() {
    val order = createDeliveryOrder()
    repository.save(order)

    repository.delete(order)

    repository.findById(order.id).shouldBeNull()
  }
}
