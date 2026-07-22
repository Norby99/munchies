package com.munchies.restaurant.domain.repository

import com.munchies.commons.Repository
import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId

/**
 * Repository interface for managing [Restaurant] entities.
 * Extends the base [Repository] with specific query operations.
 */
interface RestaurantRepository : Repository<RestaurantId, Restaurant> {

  /**
   * Finds all [Restaurant] entities associated with a specific manager ID.
   *
   * @param managerId The ID of the manager to find restaurants for.
   * @return A list of [Restaurant] entities associated with the given manager.
   */
  fun findAllByManagerId(managerId: UserId): List<Restaurant>
}
