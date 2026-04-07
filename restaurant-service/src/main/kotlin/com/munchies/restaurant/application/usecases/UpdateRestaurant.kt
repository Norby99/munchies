package com.munchies.restaurant.application.usecases

import com.munchies.restaurant.application.UseCase
import com.munchies.restaurant.domain.entity.Restaurant
import com.munchies.restaurant.domain.entity.RestaurantDetails
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.Address
import com.munchies.restaurant.domain.valueobject.Email
import com.munchies.restaurant.domain.valueobject.Phone
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.RestaurantName
import com.munchies.restaurant.domain.valueobject.UserId

data class UpdateRestaurantCommand(
  val restaurantId: String,
  val managerId: String,
  val name: String,
  val address: String,
  val phone: String,
  val email: String,
)

sealed interface UpdateRestaurantResult {
  data class Success(val restaurantId: String) : UpdateRestaurantResult
  data class ValidationError(val error: String) : UpdateRestaurantResult
  data object NotFound : UpdateRestaurantResult
  data object Unauthorized : UpdateRestaurantResult
  data object NameAlreadyExists : UpdateRestaurantResult
}

class UpdateRestaurantUseCase(
  private val repository: RestaurantRepository,
) : UseCase<UpdateRestaurantCommand, UpdateRestaurantResult> {

  override suspend operator fun invoke(command: UpdateRestaurantCommand): UpdateRestaurantResult {
    return try {
      val restaurantId = RestaurantId.of(command.restaurantId)
      val managerId = UserId.of(command.managerId)
      val restaurant = repository.findByIdSuspend(restaurantId)
        ?: return UpdateRestaurantResult.NotFound

      performUpdateIfAuthorized(command, restaurant, managerId)
    } catch (e: IllegalArgumentException) {
      UpdateRestaurantResult.ValidationError(error = e.message ?: "Validation error")
    }
  }

  private suspend fun performUpdateIfAuthorized(
    command: UpdateRestaurantCommand,
    restaurant: Restaurant,
    managerId: UserId,
  ): UpdateRestaurantResult {
    return if (restaurant.managerId != managerId) {
      UpdateRestaurantResult.Unauthorized
    } else {
      executeRestaurantUpdate(command, restaurant, managerId)
    }
  }

  private suspend fun executeRestaurantUpdate(
    command: UpdateRestaurantCommand,
    restaurant: Restaurant,
    managerId: UserId,
  ): UpdateRestaurantResult {
    val nameAlreadyTaken = repository.findByManagerId(managerId)
      .any { it.id != restaurant.id && it.details.name == RestaurantName.of(command.name) }
    if (nameAlreadyTaken) {
      return UpdateRestaurantResult.NameAlreadyExists
    }

    val newDetails = RestaurantDetails(
      name = RestaurantName.of(command.name),
      address = Address.of(command.address),
      phone = Phone.of(command.phone),
      email = Email.of(command.email),
    )
    restaurant.updateDetails(newDetails)
    repository.save(restaurant)
    return UpdateRestaurantResult.Success(restaurantId = restaurant.id.value)
  }
}
