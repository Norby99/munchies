package com.munchies.restaurant.application

import com.munchies.restaurant.application.usecases.CreateRestaurantCommand
import com.munchies.restaurant.application.usecases.CreateRestaurantResult
import com.munchies.restaurant.application.usecases.CreateRestaurantUseCase
import com.munchies.restaurant.application.usecases.DeleteRestaurantCommand
import com.munchies.restaurant.application.usecases.DeleteRestaurantResult
import com.munchies.restaurant.application.usecases.DeleteRestaurantUseCase
import com.munchies.restaurant.application.usecases.GetRestaurantDetailsQuery
import com.munchies.restaurant.application.usecases.GetRestaurantDetailsUseCase
import com.munchies.restaurant.application.usecases.UpdateRestaurantCommand
import com.munchies.restaurant.application.usecases.UpdateRestaurantResult
import com.munchies.restaurant.application.usecases.UpdateRestaurantUseCase
import com.munchies.restaurant.domain.entity.Restaurant

/**
 * Application Service for Restaurant management
 * Coordinates the execution of use cases
 */
class RestaurantApplicationService(
  private val createRestaurantUseCase: CreateRestaurantUseCase,
  private val updateRestaurantUseCase: UpdateRestaurantUseCase,
  private val getRestaurantDetailsUseCase: GetRestaurantDetailsUseCase,
  private val deleteRestaurantUseCase: DeleteRestaurantUseCase,
) : RestaurantService {

  override suspend fun createRestaurant(command: CreateRestaurantCommand): CreateRestaurantResult {
    return createRestaurantUseCase(command)
  }

  override suspend fun updateRestaurant(command: UpdateRestaurantCommand): UpdateRestaurantResult {
    return updateRestaurantUseCase(command)
  }

  override suspend fun getRestaurantDetails(query: GetRestaurantDetailsQuery): Restaurant? {
    return getRestaurantDetailsUseCase(query)
  }

  override suspend fun deleteRestaurant(command: DeleteRestaurantCommand): DeleteRestaurantResult {
    return deleteRestaurantUseCase(command)
  }
}
