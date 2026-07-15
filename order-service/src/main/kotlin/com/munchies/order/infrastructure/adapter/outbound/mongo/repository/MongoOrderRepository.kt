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

/**
 * MongoDB repository interface for performing CRUD operations on OrderDocument entities.
 *
 * This interface extends the CrudRepository provided by Micronaut Data, allowing for basic
 * CRUD operations on OrderDocument entities stored in a MongoDB database.
 */
@MongoRepository
sealed interface MongoCrudOrderRepository : CrudRepository<OrderDocument, String>

/**
 * MongoDB implementation of the OrderRepository interface.
 *
 * This class provides methods to interact with the MongoDB database for Order entities.
 * It uses the MongoCrudOrderRepository to perform CRUD operations on OrderDocument entities.
 */
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
