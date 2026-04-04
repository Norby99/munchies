package com.munchies.restaurant.application.usecases

import com.munchies.restaurant.application.UseCase
import com.munchies.restaurant.domain.entity.Restaurant
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId

data class DeleteRestaurantCommand(
  val restaurantId: String,
  val managerId: String,
)

sealed interface DeleteRestaurantResult {
  data class Success(val restaurantId: String) : DeleteRestaurantResult
  data class ValidationError(val error: String) : DeleteRestaurantResult
  data object NotFound : DeleteRestaurantResult
  data object Unauthorized : DeleteRestaurantResult
}

class DeleteRestaurantUseCase(
  private val repository: RestaurantRepository,
) : UseCase<DeleteRestaurantCommand, DeleteRestaurantResult> {
  override suspend operator fun invoke(command: DeleteRestaurantCommand): DeleteRestaurantResult {
    return try {
      repository.findByIdSuspend(RestaurantId.of(command.restaurantId))
        ?.let { toDelete ->
          validateAuthorizationAndDelete(toDelete, command)
        }
        ?: DeleteRestaurantResult.NotFound
    } catch (e: IllegalArgumentException) {
      DeleteRestaurantResult.ValidationError(error = e.message ?: "Validation error")
    }
  }

  private suspend fun validateAuthorizationAndDelete(
    restaurant: Restaurant,
    command: DeleteRestaurantCommand,
  ): DeleteRestaurantResult = if (restaurant.managerId.value != command.managerId) {
    DeleteRestaurantResult.Unauthorized
  } else {
    deleteRestaurant(RestaurantId.of(command.restaurantId))
  }

  private suspend fun deleteRestaurant(id: RestaurantId): DeleteRestaurantResult =
    repository.deleteById(id)
      .let { success ->
        if (success) {
          DeleteRestaurantResult.Success(restaurantId = id.value)
        } else {
          DeleteRestaurantResult.NotFound
        }
      }
}
