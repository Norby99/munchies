package com.munchies.restaurant.application

import com.munchies.restaurant.application.usecase.restaurant.GetDeliveryZonesCommand
import com.munchies.restaurant.application.usecase.restaurant.GetDeliveryZonesResult
import com.munchies.restaurant.application.usecase.restaurant.RestaurantUseCases
import com.munchies.restaurant.application.usecase.restaurant.UpdateDeliveryZonesCommand
import com.munchies.restaurant.application.usecase.restaurant.UpdateDeliveryZonesResult
import jakarta.inject.Singleton

/**
 * Application service for the delivery zones of a restaurant. Kept apart from
 * [RestaurantService], like [RestaurantTablesService], so that adding zones does not widen the
 * restaurant service every existing caller and test double depends on.
 */
interface RestaurantDeliveryZonesService {
  suspend fun getDeliveryZones(command: GetDeliveryZonesCommand): GetDeliveryZonesResult
  suspend fun updateDeliveryZones(command: UpdateDeliveryZonesCommand): UpdateDeliveryZonesResult
}

@Singleton
internal class RestaurantDeliveryZonesApplicationService(
  private val restaurantUseCases: RestaurantUseCases,
) : RestaurantDeliveryZonesService {

  override suspend fun getDeliveryZones(command: GetDeliveryZonesCommand): GetDeliveryZonesResult {
    return restaurantUseCases.getDeliveryZones(command)
  }

  override suspend fun updateDeliveryZones(
    command: UpdateDeliveryZonesCommand,
  ): UpdateDeliveryZonesResult {
    return restaurantUseCases.updateDeliveryZones(command)
  }
}
