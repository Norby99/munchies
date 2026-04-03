package com.munchies.restaurant.application.usecases

import com.munchies.restaurant.application.UseCase
import com.munchies.restaurant.domain.entity.Restaurant
import com.munchies.restaurant.domain.entity.RestaurantDetails
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.Address
import com.munchies.restaurant.domain.valueobject.Email
import com.munchies.restaurant.domain.valueobject.Phone
import com.munchies.restaurant.domain.valueobject.RestaurantName
import com.munchies.restaurant.domain.valueobject.UserId

data class CreateRestaurantCommand(
  val managerId: String,
  val name: String,
  val address: String,
  val phone: String,
  val email: String,
)

sealed interface CreateRestaurantResult {
  data class Success(val restaurantId: String) : CreateRestaurantResult
  data class ValidationError(val error: String) : CreateRestaurantResult
  data object NameAlreadyExists : CreateRestaurantResult
}

class CreateRestaurantUseCase(
  private val restaurantRepository: RestaurantRepository,
) : UseCase<CreateRestaurantCommand, CreateRestaurantResult> {

  override suspend operator fun invoke(command: CreateRestaurantCommand): CreateRestaurantResult {
    return try {
      val managerId = UserId.of(command.managerId)
      val details = RestaurantDetails(
        name = RestaurantName.of(command.name),
        address = Address.of(command.address),
        phone = Phone.of(command.phone),
        email = Email.of(command.email),
      )

      if (restaurantRepository.findByManagerId(managerId)
          .any { it.details.name == details.name }
      ) {
        return CreateRestaurantResult.NameAlreadyExists
      }

      val restaurant = Restaurant.create(
        managerId = managerId,
        details = details,
      )

      restaurantRepository.save(restaurant)

      CreateRestaurantResult.Success(restaurantId = restaurant.id.value)
    } catch (e: IllegalArgumentException) {
      CreateRestaurantResult.ValidationError(error = e.message ?: "Validation error")
    }
  }
}
