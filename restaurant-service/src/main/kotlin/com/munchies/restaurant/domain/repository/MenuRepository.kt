package com.munchies.restaurant.domain.repository

import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.valueobject.RestaurantId

interface MenuRepository {
  suspend fun save(menu: Menu)
  suspend fun findById(id: MenuId): Menu?
  suspend fun findByIdAndRestaurantId(id: MenuId, restaurantId: RestaurantId): Menu?
  suspend fun findAllByRestaurantId(restaurantId: RestaurantId): List<Menu>
  suspend fun delete(id: MenuId)
}
