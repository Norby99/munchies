package com.munchies.restaurant.application

import com.munchies.restaurant.application.usecases.restaurant.CreateRestaurantCommand
import com.munchies.restaurant.application.usecases.restaurant.CreateRestaurantResult
import com.munchies.restaurant.application.usecases.restaurant.CreateRestaurantUseCase
import com.munchies.restaurant.application.usecases.restaurant.DeleteRestaurantCommand
import com.munchies.restaurant.application.usecases.restaurant.DeleteRestaurantResult
import com.munchies.restaurant.application.usecases.restaurant.DeleteRestaurantUseCase
import com.munchies.restaurant.application.usecases.restaurant.GetRestaurantCommand
import com.munchies.restaurant.application.usecases.restaurant.GetRestaurantResult
import com.munchies.restaurant.application.usecases.restaurant.GetRestaurantUseCase
import com.munchies.restaurant.application.usecases.restaurant.UpdateRestaurantCommand
import com.munchies.restaurant.application.usecases.restaurant.UpdateRestaurantResult
import com.munchies.restaurant.application.usecases.restaurant.UpdateRestaurantUseCase
import jakarta.inject.Singleton

interface RestaurantService {
  suspend fun createRestaurant(command: CreateRestaurantCommand): CreateRestaurantResult
  suspend fun updateRestaurant(command: UpdateRestaurantCommand): UpdateRestaurantResult
  suspend fun deleteRestaurant(command: DeleteRestaurantCommand): DeleteRestaurantResult
  suspend fun getRestaurantDetails(command: GetRestaurantCommand): GetRestaurantResult
}

@Singleton
internal class RestaurantApplicationService(
  private val createRestaurantUseCase: CreateRestaurantUseCase,
  private val updateRestaurantUseCase: UpdateRestaurantUseCase,
  private val getRestaurantUseCase: GetRestaurantUseCase,
  private val deleteRestaurantUseCase: DeleteRestaurantUseCase,
) : RestaurantService {

  override suspend fun createRestaurant(command: CreateRestaurantCommand): CreateRestaurantResult {
    return createRestaurantUseCase(command)
  }

  override suspend fun updateRestaurant(command: UpdateRestaurantCommand): UpdateRestaurantResult {
    return updateRestaurantUseCase(command)
  }

  override suspend fun getRestaurantDetails(command: GetRestaurantCommand): GetRestaurantResult {
    return getRestaurantUseCase(command)
  }

  override suspend fun deleteRestaurant(command: DeleteRestaurantCommand): DeleteRestaurantResult {
    return deleteRestaurantUseCase(command)
  }
}
