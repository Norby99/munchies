package com.munchies.restaurant.application.usecases

import com.munchies.restaurant.application.UseCase
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId

data class DeleteRestaurantCommand(
  val restaurantId: String,
)

class DeleteRestaurantUseCase(
  private val restaurantRepository: RestaurantRepository,
) : UseCase<DeleteRestaurantCommand, Boolean> {
  override suspend operator fun invoke(command: DeleteRestaurantCommand): Boolean {
    return restaurantRepository.deleteById(RestaurantId.of(command.restaurantId))
  }
}
