package com.munchies.restaurant.application

import com.munchies.restaurant.application.usecase.restaurant.CreateRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.CreateRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.CreateRestaurantUseCase
import com.munchies.restaurant.application.usecase.restaurant.DeleteRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.DeleteRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.DeleteRestaurantUseCase
import com.munchies.restaurant.application.usecase.restaurant.GetRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.GetRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.GetRestaurantUseCase
import com.munchies.restaurant.application.usecase.restaurant.UpdateRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.UpdateRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.UpdateRestaurantUseCase

interface RestaurantService {
  suspend fun createRestaurant(command: CreateRestaurantCommand): CreateRestaurantResult
  suspend fun updateRestaurant(command: UpdateRestaurantCommand): UpdateRestaurantResult
  suspend fun deleteRestaurant(command: DeleteRestaurantCommand): DeleteRestaurantResult
  suspend fun getRestaurantDetails(command: GetRestaurantCommand): GetRestaurantResult
}

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
