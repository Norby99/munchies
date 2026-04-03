package com.munchies.restaurant.domain.repository

import com.munchies.commons.Repository
import com.munchies.restaurant.domain.entity.Restaurant
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId

/**
 * Repository interface for managing [Restaurant] entities.
 * Extends the base [Repository] with specific query operations.
 */
interface RestaurantRepository : Repository<RestaurantId, Restaurant> {

  /**
   * Asynchronously finds a restaurant by its unique identifier.
   *
   * @param id The unique identifier of the restaurant to find.
   * @return The [Restaurant] if found, or null otherwise.
   */
  suspend fun findByIdSuspend(id: RestaurantId): Restaurant?

  /**
   * Retrieves all restaurants associated with a specific manager.
   *
   * @param managerId The identifier of the manager whose restaurants are to be retrieved.
   * @return A list of [Restaurant] entities managed by the given manager.
   */
  suspend fun findByManagerId(managerId: UserId): List<Restaurant>

  /**
   * Deletes a restaurant by its unique identifier.
   *
   * @param id The unique identifier of the restaurant to delete.
   * @return true if the restaurant was successfully deleted, false if it did not exist.
   */
  suspend fun deleteById(id: RestaurantId): Boolean

  /**
   * Checks whether a restaurant exists with the given identifier.
   *
   * @param id The unique identifier to check for existence.
   * @return true if a restaurant exists with the given identifier, false otherwise.
   */
  suspend fun existsById(id: RestaurantId): Boolean
}
