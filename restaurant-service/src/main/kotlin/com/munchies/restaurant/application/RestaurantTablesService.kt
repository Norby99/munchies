package com.munchies.restaurant.application

import com.munchies.restaurant.application.usecase.restaurant.GetTablesCommand
import com.munchies.restaurant.application.usecase.restaurant.GetTablesResult
import com.munchies.restaurant.application.usecase.restaurant.RestaurantUseCases
import com.munchies.restaurant.application.usecase.restaurant.UpdateTablesCommand
import com.munchies.restaurant.application.usecase.restaurant.UpdateTablesResult
import jakarta.inject.Singleton

/**
 * Application service for the tables of a restaurant's dining room. Kept apart from
 * [RestaurantService], which covers the restaurant itself and its delivery configuration.
 */
interface RestaurantTablesService {
  suspend fun getTables(command: GetTablesCommand): GetTablesResult
  suspend fun updateTables(command: UpdateTablesCommand): UpdateTablesResult
}

@Singleton
internal class RestaurantTablesApplicationService(
  private val restaurantUseCases: RestaurantUseCases,
) : RestaurantTablesService {

  override suspend fun getTables(command: GetTablesCommand): GetTablesResult {
    return restaurantUseCases.getTables(command)
  }

  override suspend fun updateTables(command: UpdateTablesCommand): UpdateTablesResult {
    return restaurantUseCases.updateTables(command)
  }
}
