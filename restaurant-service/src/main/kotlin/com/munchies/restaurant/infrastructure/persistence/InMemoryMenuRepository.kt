package com.munchies.restaurant.infrastructure.persistence

import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import jakarta.inject.Singleton

@Singleton
class InMemoryMenuRepository : MenuRepository {
  private val menus = mutableMapOf<String, Menu>()

  override suspend fun save(menu: Menu) {
    menus[menu.id.value] = menu
  }

  override suspend fun findById(id: MenuId): Menu? {
    return menus[id.value]
  }

  override suspend fun findAllByRestaurantId(restaurantId: RestaurantId): List<Menu> {
    return menus.values.filter { it.restaurantId.value == restaurantId.value }
  }

  override suspend fun delete(id: MenuId) {
    menus.remove(id.value)
  }

  fun clear() {
    menus.clear()
  }
}
