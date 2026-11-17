package com.munchies.restaurant.application.usecase.restaurant

import com.munchies.restaurant.application.UseCase
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.DeliveryZone
import com.munchies.restaurant.domain.valueobject.restaurant.GeoPoint
import java.util.UUID

// --- Get Delivery Zones ---

data class GetDeliveryZonesCommand(
  val restaurantId: String,
)

sealed interface GetDeliveryZonesResult {
  /** [zones] is empty until the manager draws them. */
  data class Success(val zones: List<DeliveryZone>) : GetDeliveryZonesResult
  data class InvalidRestaurant(val error: String) : GetDeliveryZonesResult
  data object RestaurantNotFound : GetDeliveryZonesResult
}

class GetDeliveryZonesUseCase(
  private val repository: RestaurantRepository,
) : UseCase<GetDeliveryZonesCommand, GetDeliveryZonesResult> {

  override suspend operator fun invoke(command: GetDeliveryZonesCommand): GetDeliveryZonesResult =
    runCatching { RestaurantId.of(command.restaurantId) }.fold(
      onSuccess = { zonesOf(it) },
      onFailure = { GetDeliveryZonesResult.InvalidRestaurant(it.message.orEmpty()) },
    )

  private suspend fun zonesOf(restaurantId: RestaurantId): GetDeliveryZonesResult =
    repository.findById(restaurantId)
      ?.let { GetDeliveryZonesResult.Success(it.deliveryZones) }
      ?: GetDeliveryZonesResult.RestaurantNotFound
}

// --- Update Delivery Zones ---

/** A vertex of a zone boundary as requested by the manager, not validated yet. */
data class ZoneVertexSpec(
  val lat: Double,
  val lng: Double,
)

/**
 * A zone as requested by the manager, not validated yet.
 *
 * @property id The zone's id, or blank for a zone drawn since the last save: one is generated.
 */
data class DeliveryZoneSpec(
  val id: String,
  val name: String,
  val boundary: List<ZoneVertexSpec>,
)

/** Replaces every delivery zone of the restaurant with [zones]. */
data class UpdateDeliveryZonesCommand(
  val managerId: String,
  val restaurantId: String,
  val zones: List<DeliveryZoneSpec>,
)

sealed interface UpdateDeliveryZonesResult {
  data class Success(val zones: List<DeliveryZone>) : UpdateDeliveryZonesResult
  data class InvalidRestaurant(val error: String) : UpdateDeliveryZonesResult
  data class InvalidZones(val error: String) : UpdateDeliveryZonesResult
  data object NotFound : UpdateDeliveryZonesResult
  data object Unauthorized : UpdateDeliveryZonesResult
}

class UpdateDeliveryZonesUseCase(
  private val repository: RestaurantRepository,
) : UseCase<UpdateDeliveryZonesCommand, UpdateDeliveryZonesResult> {

  // Inputs are validated in this order: restaurant id, caller, zones; the first invalid one
  // decides the result, before the repository is touched.
  override suspend operator fun invoke(
    command: UpdateDeliveryZonesCommand,
  ): UpdateDeliveryZonesResult {
    val restaurantId = runCatching { RestaurantId.of(command.restaurantId) }
    val managerId = runCatching { UserId.of(command.managerId) }.getOrNull()
    val zones = runCatching { command.zones.map { it.toZone() } }
    return when {
      restaurantId.isFailure ->
        UpdateDeliveryZonesResult.InvalidRestaurant(
          restaurantId.exceptionOrNull()?.message.orEmpty(),
        )
      managerId == null -> UpdateDeliveryZonesResult.Unauthorized
      zones.isFailure ->
        UpdateDeliveryZonesResult.InvalidZones(zones.exceptionOrNull()?.message.orEmpty())
      else -> update(restaurantId.getOrThrow(), managerId, zones.getOrThrow())
    }
  }

  private suspend fun update(
    restaurantId: RestaurantId,
    managerId: UserId,
    zones: List<DeliveryZone>,
  ): UpdateDeliveryZonesResult {
    val restaurant = repository.findById(restaurantId)
    return when {
      restaurant == null -> UpdateDeliveryZonesResult.NotFound
      restaurant.managerId != managerId -> UpdateDeliveryZonesResult.Unauthorized
      // The zones as a whole are checked by the aggregate (unique ids and names, how many).
      else -> runCatching { restaurant.updateDeliveryZones(zones) }.fold(
        onSuccess = {
          repository.update(restaurant)
          UpdateDeliveryZonesResult.Success(restaurant.deliveryZones)
        },
        onFailure = { UpdateDeliveryZonesResult.InvalidZones(it.message.orEmpty()) },
      )
    }
  }

  private fun DeliveryZoneSpec.toZone(): DeliveryZone = DeliveryZone(
    id = id.trim().ifEmpty { UUID.randomUUID().toString() },
    name = name.trim(),
    boundary = boundary.map { GeoPoint(it.lat, it.lng) },
  )
}
