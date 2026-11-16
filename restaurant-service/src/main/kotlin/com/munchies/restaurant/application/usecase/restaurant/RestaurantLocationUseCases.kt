package com.munchies.restaurant.application.usecase.restaurant

import com.munchies.restaurant.application.UseCase
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.GeoPoint

// --- Get Restaurant Location ---

data class GetRestaurantLocationCommand(
  val restaurantId: String,
)

sealed interface GetRestaurantLocationResult {
  data class Success(val location: GeoPoint) : GetRestaurantLocationResult
  data class InvalidRestaurant(val error: String) : GetRestaurantLocationResult
  data object RestaurantNotFound : GetRestaurantLocationResult
  data object NotConfigured : GetRestaurantLocationResult
}

class GetRestaurantLocationUseCase(
  private val repository: RestaurantRepository,
) : UseCase<GetRestaurantLocationCommand, GetRestaurantLocationResult> {

  override suspend operator fun invoke(
    command: GetRestaurantLocationCommand,
  ): GetRestaurantLocationResult = runCatching { RestaurantId.of(command.restaurantId) }.fold(
    onSuccess = { locationOf(it) },
    onFailure = { GetRestaurantLocationResult.InvalidRestaurant(it.message.orEmpty()) },
  )

  private suspend fun locationOf(restaurantId: RestaurantId): GetRestaurantLocationResult {
    val restaurant = repository.findById(restaurantId)
    val location = restaurant?.location
    return when {
      restaurant == null -> GetRestaurantLocationResult.RestaurantNotFound
      location == null -> GetRestaurantLocationResult.NotConfigured
      else -> GetRestaurantLocationResult.Success(location)
    }
  }
}

// --- Update Restaurant Location ---

data class UpdateRestaurantLocationCommand(
  val managerId: String,
  val restaurantId: String,
  val lat: Double,
  val lng: Double,
)

sealed interface UpdateRestaurantLocationResult {
  data class Success(val location: GeoPoint) : UpdateRestaurantLocationResult
  data class InvalidRestaurant(val error: String) : UpdateRestaurantLocationResult
  data class InvalidLocation(val error: String) : UpdateRestaurantLocationResult
  data object NotFound : UpdateRestaurantLocationResult
  data object Unauthorized : UpdateRestaurantLocationResult
}

class UpdateRestaurantLocationUseCase(
  private val repository: RestaurantRepository,
) : UseCase<UpdateRestaurantLocationCommand, UpdateRestaurantLocationResult> {

  // Inputs are validated in this order: restaurant id, caller, coordinates; the first invalid one
  // decides the result, before the repository is touched.
  override suspend operator fun invoke(
    command: UpdateRestaurantLocationCommand,
  ): UpdateRestaurantLocationResult {
    val restaurantId = runCatching { RestaurantId.of(command.restaurantId) }
    val managerId = runCatching { UserId.of(command.managerId) }.getOrNull()
    val location = runCatching { GeoPoint(command.lat, command.lng) }
    return when {
      restaurantId.isFailure ->
        UpdateRestaurantLocationResult.InvalidRestaurant(
          restaurantId.exceptionOrNull()?.message.orEmpty(),
        )
      managerId == null -> UpdateRestaurantLocationResult.Unauthorized
      location.isFailure ->
        UpdateRestaurantLocationResult.InvalidLocation(
          location.exceptionOrNull()?.message.orEmpty(),
        )
      else -> update(restaurantId.getOrThrow(), managerId, location.getOrThrow())
    }
  }

  private suspend fun update(
    restaurantId: RestaurantId,
    managerId: UserId,
    location: GeoPoint,
  ): UpdateRestaurantLocationResult {
    val restaurant = repository.findById(restaurantId)
    return when {
      restaurant == null -> UpdateRestaurantLocationResult.NotFound
      restaurant.managerId != managerId -> UpdateRestaurantLocationResult.Unauthorized
      else -> {
        restaurant.updateLocation(location)
        repository.update(restaurant)
        UpdateRestaurantLocationResult.Success(location)
      }
    }
  }
}
