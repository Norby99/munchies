package com.munchies.restaurant.application

import com.munchies.restaurant.application.usecase.restaurant.CreateRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.CreateRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.DeleteRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.DeleteRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.GetManagerRestaurantsCommand
import com.munchies.restaurant.application.usecase.restaurant.GetManagerRestaurantsResult
import com.munchies.restaurant.application.usecase.restaurant.GetRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.GetRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.RestaurantUseCases
import com.munchies.restaurant.application.usecase.restaurant.UpdateRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.UpdateRestaurantResult
import jakarta.inject.Singleton

interface RestaurantService {
  suspend fun createRestaurant(command: CreateRestaurantCommand): CreateRestaurantResult
  suspend fun updateRestaurant(command: UpdateRestaurantCommand): UpdateRestaurantResult
  suspend fun deleteRestaurant(command: DeleteRestaurantCommand): DeleteRestaurantResult
  suspend fun getRestaurant(command: GetRestaurantCommand): GetRestaurantResult
  suspend fun getManagerRestaurants(
    command: GetManagerRestaurantsCommand,
  ): GetManagerRestaurantsResult
}

@Singleton
internal class RestaurantApplicationService(
  private val restaurantUseCases: RestaurantUseCases,
) : RestaurantService {

  override suspend fun createRestaurant(command: CreateRestaurantCommand): CreateRestaurantResult {
    return restaurantUseCases.create(command)
  }

  override suspend fun updateRestaurant(command: UpdateRestaurantCommand): UpdateRestaurantResult {
    return restaurantUseCases.update(command)
  }

  override suspend fun deleteRestaurant(command: DeleteRestaurantCommand): DeleteRestaurantResult {
    return restaurantUseCases.delete(command)
  }

  override suspend fun getRestaurant(command: GetRestaurantCommand): GetRestaurantResult {
    return restaurantUseCases.getDetails(command)
  }

  override suspend fun getManagerRestaurants(
    command: GetManagerRestaurantsCommand,
  ): GetManagerRestaurantsResult {
    return restaurantUseCases.getManagerRestaurants(command)
  }
}
