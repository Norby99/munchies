package com.munchies.restaurant.application.usecases

import com.munchies.restaurant.application.UseCase
import com.munchies.restaurant.domain.entity.Restaurant
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId

data class GetRestaurantCommand(
  val restaurantId: String,
)

sealed interface GetRestaurantResult {
  data class Success(val restaurant: Restaurant) : GetRestaurantResult
  data object NotFound : GetRestaurantResult
}

class GetRestaurantUseCase(
  private val restaurantRepository: RestaurantRepository,
) : UseCase<GetRestaurantCommand, GetRestaurantResult> {
  override suspend operator fun invoke(command: GetRestaurantCommand): GetRestaurantResult {
    return restaurantRepository.findByIdSuspend(RestaurantId.of(command.restaurantId))
      ?.let { GetRestaurantResult.Success(it) }
      ?: GetRestaurantResult.NotFound
  }
}
