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
  data class InvalidRestaurant(val error: String) : GetRestaurantResult
  data object NotFound : GetRestaurantResult
}

class GetRestaurantUseCase(
  private val repository: RestaurantRepository,
) : UseCase<GetRestaurantCommand, GetRestaurantResult> {
  override suspend operator fun invoke(command: GetRestaurantCommand): GetRestaurantResult {
    return runCatching {
      repository.findById(RestaurantId.of(command.restaurantId))
        ?.let { GetRestaurantResult.Success(it) }
        ?: GetRestaurantResult.NotFound
    }.getOrElse { GetRestaurantResult.InvalidRestaurant(it.message.orEmpty()) }
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
  private val repository: RestaurantRepository,
) : UseCase<GetManagerRestaurantsCommand, GetManagerRestaurantsResult> {
  override suspend operator fun invoke(
    command: GetManagerRestaurantsCommand,
  ): GetManagerRestaurantsResult {
    return runCatching {
      val managerId = UserId.of(command.managerId)
      val restaurants = repository.findAllByManagerId(managerId)
      GetManagerRestaurantsResult.Success(restaurants)
    }.getOrElse { GetManagerRestaurantsResult.ValidationError(it.message.orEmpty()) }
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
  data class InvalidRestaurant(val error: String) : CreateRestaurantResult
  data object NameAlreadyExists : CreateRestaurantResult
}

class CreateRestaurantUseCase(
  private val repository: RestaurantRepository,
) : UseCase<CreateRestaurantCommand, CreateRestaurantResult> {

  override suspend operator fun invoke(command: CreateRestaurantCommand): CreateRestaurantResult {
    return runCatching {
      val managerId = UserId.of(command.managerId)
      val details = RestaurantDetails(
        name = RestaurantName.of(command.name),
        address = Address.of(command.address),
        phone = Phone.of(command.phone),
        email = Email.of(command.email),
      )

      if (repository.findAllByManagerId(managerId)
          .any { it.details.name == details.name }
      ) {
        return CreateRestaurantResult.NameAlreadyExists
      }

      val restaurant = Restaurant.create(
        managerId = managerId,
        details = details,
      )

      repository.save(restaurant)

      CreateRestaurantResult.Success(restaurantId = restaurant.id.value)
    }.getOrElse { CreateRestaurantResult.InvalidRestaurant(it.message.orEmpty()) }
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
  data class InvalidRestaurant(val error: String) : UpdateRestaurantResult
  data object NotFound : UpdateRestaurantResult
  data object Unauthorized : UpdateRestaurantResult
  data object NameAlreadyExists : UpdateRestaurantResult
}

class UpdateRestaurantUseCase(
  private val repository: RestaurantRepository,
) : UseCase<UpdateRestaurantCommand, UpdateRestaurantResult> {

  override suspend operator fun invoke(command: UpdateRestaurantCommand): UpdateRestaurantResult {
    return runCatching {
      val restaurantId = RestaurantId.of(command.restaurantId)
      val managerId = UserId.of(command.managerId)
      val restaurant = repository.findById(restaurantId)
      when {
        restaurant == null -> UpdateRestaurantResult.NotFound
        restaurant.managerId != managerId -> UpdateRestaurantResult.Unauthorized
        repository.findAllByManagerId(managerId)
          .any { it.id != restaurant.id && it.details.name == RestaurantName.of(command.name) }
        -> UpdateRestaurantResult.NameAlreadyExists
        else -> {
          val newDetails = RestaurantDetails(
            name = RestaurantName.of(command.name),
            address = Address.of(command.address),
            phone = Phone.of(command.phone),
            email = Email.of(command.email),
          )
          restaurant.updateDetails(newDetails)
          repository.save(restaurant)
          UpdateRestaurantResult.Success(restaurantId = restaurant.id.value)
        }
      }
    }.getOrElse { UpdateRestaurantResult.InvalidRestaurant(it.message.orEmpty()) }
  }
}

// --- Delete Restaurant ---

data class DeleteRestaurantCommand(
  val restaurantId: String,
  val managerId: String,
)

sealed interface DeleteRestaurantResult {
  data class Success(val restaurantId: String) : DeleteRestaurantResult
  data class InvalidRestaurant(val error: String) : DeleteRestaurantResult
  data object NotFound : DeleteRestaurantResult
  data object Unauthorized : DeleteRestaurantResult
}

class DeleteRestaurantUseCase(
  private val repository: RestaurantRepository,
) : UseCase<DeleteRestaurantCommand, DeleteRestaurantResult> {
  override suspend operator fun invoke(command: DeleteRestaurantCommand): DeleteRestaurantResult {
    return runCatching {
      val restaurant = repository.findById(RestaurantId.of(command.restaurantId))
      when {
        restaurant == null -> DeleteRestaurantResult.NotFound
        restaurant.managerId.value != command.managerId -> DeleteRestaurantResult.Unauthorized
        else -> {
          repository.delete(restaurant)
          DeleteRestaurantResult.Success(restaurantId = restaurant.id.value)
        }
      }
    }.getOrElse { DeleteRestaurantResult.InvalidRestaurant(it.message.orEmpty()) }
  }
}
