package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.RestaurantDocument
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory.RestaurantDocumentFactory.toDocument
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory.RestaurantDocumentFactory.toDomain
import io.micronaut.context.annotation.Requires
import io.micronaut.data.mongodb.annotation.MongoRepository
import io.micronaut.data.repository.CrudRepository
import jakarta.inject.Singleton

@MongoRepository
sealed interface MongoCrudRestaurantRepository : CrudRepository<RestaurantDocument, String>

@Singleton
@Requires(env = ["prod"])
class MongoRestaurantRepository(
  private val repository: MongoCrudRestaurantRepository,
) : RestaurantRepository {

  override fun findById(id: RestaurantId): Restaurant? =
    repository.findById(id.value).map { it.toDomain() }.orElse(null)

  override fun save(entity: Restaurant) {
    repository.save(entity.toDocument())
  }

  override fun update(entity: Restaurant) {
    repository.update(entity.toDocument())
  }

  override fun delete(entity: Restaurant) {
    repository.delete(entity.toDocument())
  }

  override fun findAllByManagerId(managerId: UserId): List<Restaurant> =
    repository.findAll().filter { it.managerId == managerId.value }.map { it.toDomain() }
}
