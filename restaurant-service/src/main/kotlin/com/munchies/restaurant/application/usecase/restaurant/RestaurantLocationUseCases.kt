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
  ): GetRestaurantLocationResult {
    val restaurantId = runCatching { RestaurantId.of(command.restaurantId) }
      .getOrElse { return GetRestaurantLocationResult.InvalidRestaurant(it.message.orEmpty()) }
    val restaurant = repository.findById(restaurantId)
      ?: return GetRestaurantLocationResult.RestaurantNotFound
    val location = restaurant.location
      ?: return GetRestaurantLocationResult.NotConfigured
    return GetRestaurantLocationResult.Success(location)
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

  override suspend operator fun invoke(
    command: UpdateRestaurantLocationCommand,
  ): UpdateRestaurantLocationResult {
    val restaurantId = runCatching { RestaurantId.of(command.restaurantId) }
      .getOrElse { return UpdateRestaurantLocationResult.InvalidRestaurant(it.message.orEmpty()) }
    val managerId = runCatching { UserId.of(command.managerId) }
      .getOrElse { return UpdateRestaurantLocationResult.Unauthorized }
    val location = runCatching { GeoPoint(command.lat, command.lng) }
      .getOrElse { return UpdateRestaurantLocationResult.InvalidLocation(it.message.orEmpty()) }

    val restaurant = repository.findById(restaurantId)
      ?: return UpdateRestaurantLocationResult.NotFound
    if (restaurant.managerId != managerId) return UpdateRestaurantLocationResult.Unauthorized

    restaurant.updateLocation(location)
    repository.update(restaurant)
    return UpdateRestaurantLocationResult.Success(location)
  }
}
