package com.munchies.restaurant.domain.repository

import com.munchies.commons.Repository
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.valueobject.RestaurantId

/**
 * Repository interface for managing [Menu] entities.
 * Extends the base [Repository] with specific query operations.
 */
interface MenuRepository : Repository<MenuId, Menu> {
  /**
   * Finds a [Menu] by its ID and associated [RestaurantId].
   *
   * @param id The ID of the menu to find.
   * @param restaurantId The ID of the restaurant associated with the menu.
   * @return The found [Menu] or null if not found.
   */
  fun findByIdAndRestaurantId(id: MenuId, restaurantId: RestaurantId): Menu?

  /**
   * Finds all [Menu] entities associated with a specific [RestaurantId].
   *
   * @param restaurantId The ID of the restaurant to find menus for.
   * @return A list of [Menu] entities associated with the given restaurant.
   */
  fun findAllByRestaurantId(restaurantId: RestaurantId): List<Menu>
}
