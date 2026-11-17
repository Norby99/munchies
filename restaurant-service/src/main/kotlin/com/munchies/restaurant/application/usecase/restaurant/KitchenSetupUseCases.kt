package com.munchies.restaurant.application.usecase.restaurant

import com.munchies.restaurant.application.UseCase
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.CookStation
import com.munchies.restaurant.domain.valueobject.restaurant.KitchenRoute
import com.munchies.restaurant.domain.valueobject.restaurant.KitchenSetup

// --- Get Kitchen Setup ---

data class GetKitchenSetupCommand(
  val restaurantId: String,
)

sealed interface GetKitchenSetupResult {
  data class Success(val setup: KitchenSetup) : GetKitchenSetupResult
  data class InvalidRestaurant(val error: String) : GetKitchenSetupResult
  data object RestaurantNotFound : GetKitchenSetupResult

  /** The restaurant exists, but its manager has not described the kitchen yet. */
  data object NotConfigured : GetKitchenSetupResult
}

class GetKitchenSetupUseCase(
  private val repository: RestaurantRepository,
) : UseCase<GetKitchenSetupCommand, GetKitchenSetupResult> {

  override suspend operator fun invoke(command: GetKitchenSetupCommand): GetKitchenSetupResult =
    runCatching { RestaurantId.of(command.restaurantId) }.fold(
      onSuccess = { setupOf(it) },
      onFailure = { GetKitchenSetupResult.InvalidRestaurant(it.message.orEmpty()) },
    )

  private suspend fun setupOf(restaurantId: RestaurantId): GetKitchenSetupResult {
    val restaurant = repository.findById(restaurantId)
    val setup = restaurant?.kitchenSetup
    return when {
      restaurant == null -> GetKitchenSetupResult.RestaurantNotFound
      setup == null -> GetKitchenSetupResult.NotConfigured
      else -> GetKitchenSetupResult.Success(setup)
    }
  }
}

// --- Update Kitchen Setup ---

/** A cook station as requested by the manager, not validated yet. */
data class CookStationSpec(
  val name: String,
  val places: Int,
)

/**
 * Replaces the kitchen description of the restaurant.
 *
 * @property route The name of a [KitchenRoute], e.g. `PREP_THEN_COOK`.
 */
data class UpdateKitchenSetupCommand(
  val managerId: String,
  val restaurantId: String,
  val route: String,
  val prepWorkers: Int,
  val cookStations: List<CookStationSpec>,
  val prepMinutes: Double,
  val loadingMinutes: Double,
  val cookMinutes: Double,
)

sealed interface UpdateKitchenSetupResult {
  data class Success(val setup: KitchenSetup) : UpdateKitchenSetupResult
  data class InvalidRestaurant(val error: String) : UpdateKitchenSetupResult
  data class InvalidSetup(val error: String) : UpdateKitchenSetupResult
  data object NotFound : UpdateKitchenSetupResult
  data object Unauthorized : UpdateKitchenSetupResult
}

class UpdateKitchenSetupUseCase(
  private val repository: RestaurantRepository,
) : UseCase<UpdateKitchenSetupCommand, UpdateKitchenSetupResult> {

  // Inputs are validated in this order: restaurant id, caller, setup; the first invalid one
  // decides the result, before the repository is touched.
  override suspend operator fun invoke(
    command: UpdateKitchenSetupCommand,
  ): UpdateKitchenSetupResult {
    val restaurantId = runCatching { RestaurantId.of(command.restaurantId) }
    val managerId = runCatching { UserId.of(command.managerId) }.getOrNull()
    val setup = runCatching { command.toSetup() }
    return when {
      restaurantId.isFailure ->
        UpdateKitchenSetupResult.InvalidRestaurant(
          restaurantId.exceptionOrNull()?.message.orEmpty(),
        )
      managerId == null -> UpdateKitchenSetupResult.Unauthorized
      setup.isFailure ->
        UpdateKitchenSetupResult.InvalidSetup(setup.exceptionOrNull()?.message.orEmpty())
      else -> update(restaurantId.getOrThrow(), managerId, setup.getOrThrow())
    }
  }

  private suspend fun update(
    restaurantId: RestaurantId,
    managerId: UserId,
    setup: KitchenSetup,
  ): UpdateKitchenSetupResult {
    val restaurant = repository.findById(restaurantId)
    return when {
      restaurant == null -> UpdateKitchenSetupResult.NotFound
      restaurant.managerId != managerId -> UpdateKitchenSetupResult.Unauthorized
      else -> {
        restaurant.updateKitchenSetup(setup)
        repository.update(restaurant)
        UpdateKitchenSetupResult.Success(setup)
      }
    }
  }

  private fun UpdateKitchenSetupCommand.toSetup(): KitchenSetup = KitchenSetup(
    route = parseRoute(route),
    prepWorkers = prepWorkers,
    cookStations = cookStations.map { CookStation(name = it.name.trim(), places = it.places) },
    prepMinutes = prepMinutes,
    loadingMinutes = loadingMinutes,
    cookMinutes = cookMinutes,
  )

  private fun parseRoute(route: String): KitchenRoute =
    KitchenRoute.entries.firstOrNull { it.name == route.trim() }
      ?: throw IllegalArgumentException(
        "unknown route \"$route\": expected one of ${KitchenRoute.entries.joinToString()}",
      )
}
