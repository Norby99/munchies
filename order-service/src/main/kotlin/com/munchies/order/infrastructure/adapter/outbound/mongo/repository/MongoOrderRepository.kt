package com.munchies.order.infrastructure.adapter.outbound.mongo.repository

import com.munchies.order.domain.model.Order
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.ports.OrderRepository
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.factory.OrderDocumentFactory.toDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.factory.OrderDocumentFactory.toNullableDomain
import io.micronaut.context.annotation.Requires
import io.micronaut.data.mongodb.annotation.MongoRepository
import io.micronaut.data.repository.CrudRepository
import jakarta.inject.Singleton

@MongoRepository
sealed interface MongoCrudOrderRepository : CrudRepository<OrderDocument, String>

@Singleton
@Requires(env = ["prod"])
class MongoOrderRepository(
  private val repository: MongoCrudOrderRepository,
) : OrderRepository {

  override fun findById(id: OrderId): Order? = repository.findById(id.value).map {
    it.toNullableDomain()
  }.orElse(null)

  override fun save(entity: Order) {
    repository.save(entity.toDocument())
  }

  override fun update(entity: Order) {
    repository.update(entity.toDocument())
  }

  override fun delete(entity: Order) {
    repository.delete(entity.toDocument())
  }
}
