package com.munchies.restaurant.infrastructure.persistence

import com.munchies.commons.repository.InMemoryRepository
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

@Singleton
@Requires(env = ["test"])
class InMemoryMenuRepository : MenuRepository, InMemoryRepository<MenuId, Menu>() {
  override fun findByIdAndRestaurantId(id: MenuId, restaurantId: RestaurantId): Menu? =
    findById(id)?.takeIf { it.restaurantId == restaurantId }

  override fun findAllByRestaurantId(restaurantId: RestaurantId): List<Menu> =
    findAllByPredicate { it.restaurantId == restaurantId }
}
