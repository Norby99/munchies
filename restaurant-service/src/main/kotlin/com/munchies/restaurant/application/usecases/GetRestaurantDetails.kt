package com.munchies.restaurant.application.usecases

import com.munchies.restaurant.application.UseCase
import com.munchies.restaurant.domain.entity.Restaurant
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId

data class GetRestaurantDetailsQuery(
  val restaurantId: String,
)

class GetRestaurantDetailsUseCase(
  private val restaurantRepository: RestaurantRepository,
) : UseCase<GetRestaurantDetailsQuery, Restaurant?> {
  override suspend operator fun invoke(command: GetRestaurantDetailsQuery): Restaurant? {
    return restaurantRepository.findByIdSuspend(RestaurantId.of(command.restaurantId))
  }
}
