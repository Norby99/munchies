package com.munchies.restaurant.application.usecase.restaurant

import com.munchies.restaurant.application.UseCase
import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.aggregate.RestaurantDetails
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import jakarta.inject.Singleton

@Singleton
data class RestaurantUseCases(val repository: RestaurantRepository) {
  val create = CreateRestaurantUseCase(repository)
  val update = UpdateRestaurantUseCase(repository)
  val delete = DeleteRestaurantUseCase(repository)
  val getDetails = GetRestaurantUseCase(repository)
  val getManagerRestaurants = GetManagerRestaurantsUseCase(repository)
}

// --- Get Restaurant ---

data class GetRestaurantCommand(
  val restaurantId: String,
)

sealed interface GetRestaurantResult {
  data class Success(val restaurant: Restaurant) : GetRestaurantResult
  data class ValidationError(val error: String) : GetRestaurantResult
  data object NotFound : GetRestaurantResult
}

class GetRestaurantUseCase(
  private val restaurantRepository: RestaurantRepository,
) : UseCase<GetRestaurantCommand, GetRestaurantResult> {
  override suspend operator fun invoke(command: GetRestaurantCommand): GetRestaurantResult {
    return try {
      restaurantRepository.findByIdSuspend(RestaurantId.of(command.restaurantId))
        ?.let { GetRestaurantResult.Success(it) }
        ?: GetRestaurantResult.NotFound
    } catch (e: IllegalArgumentException) {
      GetRestaurantResult.ValidationError(error = e.message ?: "Validation error")
    }
  }
}

// --- Get Manager Restaurants ---

data class GetManagerRestaurantsCommand(
  val managerId: String,
)

sealed interface GetManagerRestaurantsResult {
  data class Success(val restaurants: List<Restaurant>) : GetManagerRestaurantsResult
  data class ValidationError(val error: String) : GetManagerRestaurantsResult
}

class GetManagerRestaurantsUseCase(
  private val restaurantRepository: RestaurantRepository,
) : UseCase<GetManagerRestaurantsCommand, GetManagerRestaurantsResult> {
  override suspend operator fun invoke(
    command: GetManagerRestaurantsCommand,
  ): GetManagerRestaurantsResult {
    return try {
      val managerId = UserId.of(command.managerId)
      val restaurants = restaurantRepository.findByManagerId(managerId)
      GetManagerRestaurantsResult.Success(restaurants)
    } catch (e: IllegalArgumentException) {
      GetManagerRestaurantsResult.ValidationError(error = e.message ?: "Validation error")
    }
  }
}

// --- Create Restaurant ---

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

// --- Update Restaurant ---

data class UpdateRestaurantCommand(
  val managerId: String,
  val restaurantId: String,
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

// --- Delete Restaurant ---

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
