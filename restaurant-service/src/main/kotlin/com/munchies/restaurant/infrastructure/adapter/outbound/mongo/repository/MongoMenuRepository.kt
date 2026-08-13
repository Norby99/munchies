package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository

import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.MenuDocument
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory.MenuDocumentFactory
import io.micronaut.context.annotation.Requires
import io.micronaut.data.mongodb.annotation.MongoRepository
import io.micronaut.data.repository.CrudRepository
import jakarta.inject.Singleton

@MongoRepository
sealed interface MongoCrudMenuRepository : CrudRepository<MenuDocument, String>

@Singleton
@Requires(env = ["prod"])
class MongoMenuRepository(
  private val repository: MongoCrudMenuRepository,
  private val menuDocumentFactory: MenuDocumentFactory,
) : MenuRepository {

  override fun save(entity: Menu) {
    repository.save(menuDocumentFactory.toDocument(entity))
  }

  override fun update(entity: Menu) {
    repository.update(menuDocumentFactory.toDocument(entity))
  }

  override fun delete(entity: Menu) {
    repository.delete(menuDocumentFactory.toDocument(entity))
  }

  override fun findById(id: MenuId): Menu? =
    repository.findById(id.value).map { menuDocumentFactory.toDomain(it) }.orElse(null)

  override fun findByIdAndRestaurantId(id: MenuId, restaurantId: RestaurantId): Menu? =
    repository.findById(id.value)
      .map { menuDocumentFactory.toDomain(it) }
      .orElse(null)
      ?.takeIf { it.restaurantId == restaurantId }

  override fun findAllByRestaurantId(restaurantId: RestaurantId): List<Menu> = repository.findAll()
    .filter { it.restaurantId == restaurantId.value }.map { menuDocumentFactory.toDomain(it) }
}
