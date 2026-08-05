package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository

import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.MenuDocument
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory.MenuDocumentFactory.toDocument
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory.MenuDocumentFactory.toDomain
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
) : MenuRepository {

  override fun save(entity: Menu) {
    repository.save(entity.toDocument())
  }

  override fun update(entity: Menu) {
    TODO("Not yet implemented")
  }

  override fun delete(entity: Menu) {
    TODO("Not yet implemented")
  }

  override fun findById(id: MenuId): Menu? =
    repository.findById(id.value).map { it.toDomain() }.orElse(null)

  override fun findByIdAndRestaurantId(id: MenuId, restaurantId: RestaurantId): Menu? =
    repository.findById(id.value)
      .map { it.toDomain() }
      .orElse(null)
      ?.takeIf { it.restaurantId == restaurantId }

  override fun findAllByRestaurantId(restaurantId: RestaurantId): List<Menu> = repository.findAll()
    .filter { it.restaurantId == restaurantId.value }.map { it.toDomain() }
}
