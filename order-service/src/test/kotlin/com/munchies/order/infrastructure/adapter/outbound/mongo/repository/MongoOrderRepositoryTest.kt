package com.munchies.order.infrastructure.adapter.outbound.mongo.repository

import com.munchies.order.domain.model.OrderId
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.infrastructure.adapter.outbound.mongo.factory.OrderDocumentFactory.toDocument
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.*
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class MongoOrderRepositoryTest {

  val order = createDeliveryOrder()
  val orderId = order.id
  val realOrderDoc = order.toDocument()

  @Test
  fun `mongo repository correctly finds existing order by id`() {
    val crudOrderRepository = mock<MongoCrudOrderRepository> {
      on { findById(orderId.value) } doReturn Optional.of(
        realOrderDoc,
      )
    }
    val mongoOrderRepository = MongoOrderRepository(crudOrderRepository)

    val order = mongoOrderRepository.findById(orderId)

    order shouldNotBe null
    order?.id?.shouldBeEqual(orderId) ?: fail()
    verify(crudOrderRepository).findById(orderId.value)
  }

  @Test
  fun `mongo repository correctly return null at non-existing order find`() {
    val crudOrderRepository = mock<MongoCrudOrderRepository> {
      on { findById("non-id") } doReturn Optional.empty()
    }
    val mongoOrderRepository = MongoOrderRepository(crudOrderRepository)

    val order = mongoOrderRepository.findById(OrderId("non-${orderId.value}"))

    order shouldBe null
    verify(crudOrderRepository).findById("non-${orderId.value}")
  }

  @Test
  fun save() {
    val crudOrderRepository = mock<MongoCrudOrderRepository>()
    val mongoOrderRepository = MongoOrderRepository(crudOrderRepository)
    val order = createDeliveryOrder()

    mongoOrderRepository.save(order)

    verify(crudOrderRepository).save(any())
  }

  @Test
  fun update() {
    val crudOrderRepository = mock<MongoCrudOrderRepository>()
    val mongoOrderRepository = MongoOrderRepository(crudOrderRepository)
    val order = createDeliveryOrder()

    mongoOrderRepository.update(order)

    verify(crudOrderRepository).update(any())
  }

  @Test
  fun delete() {
    val crudOrderRepository = mock<MongoCrudOrderRepository>()
    val mongoOrderRepository = MongoOrderRepository(crudOrderRepository)
    val order = createDeliveryOrder()

    mongoOrderRepository.delete(order)

    verify(crudOrderRepository).delete(any())
  }
}
