package com.munchies.restaurant.application

import com.munchies.restaurant.application.usecase.restaurant.GetKitchenSetupCommand
import com.munchies.restaurant.application.usecase.restaurant.GetKitchenSetupResult
import com.munchies.restaurant.application.usecase.restaurant.RestaurantUseCases
import com.munchies.restaurant.application.usecase.restaurant.UpdateKitchenSetupCommand
import com.munchies.restaurant.application.usecase.restaurant.UpdateKitchenSetupResult
import jakarta.inject.Singleton

/**
 * Application service for the kitchen description of a restaurant. Kept apart from
 * [RestaurantService], like [RestaurantDeliveryZonesService], so that adding it does not widen
 * the restaurant service every existing caller and test double depends on.
 */
interface RestaurantKitchenService {
  suspend fun getKitchenSetup(command: GetKitchenSetupCommand): GetKitchenSetupResult
  suspend fun updateKitchenSetup(command: UpdateKitchenSetupCommand): UpdateKitchenSetupResult
}

@Singleton
internal class RestaurantKitchenApplicationService(
  private val restaurantUseCases: RestaurantUseCases,
) : RestaurantKitchenService {

  override suspend fun getKitchenSetup(command: GetKitchenSetupCommand): GetKitchenSetupResult {
    return restaurantUseCases.getKitchenSetup(command)
  }

  override suspend fun updateKitchenSetup(
    command: UpdateKitchenSetupCommand,
  ): UpdateKitchenSetupResult {
    return restaurantUseCases.updateKitchenSetup(command)
  }
}
