package com.munchies.restaurant.application.usecase.restaurant

import com.munchies.restaurant.application.UseCase
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.CapacityPolicy
import java.time.LocalTime

// --- Get Capacity Policy ---

data class GetCapacityPolicyCommand(
  val restaurantId: String,
)

sealed interface GetCapacityPolicyResult {
  data class Success(val policy: CapacityPolicy) : GetCapacityPolicyResult
  data class InvalidRestaurant(val error: String) : GetCapacityPolicyResult
  data object RestaurantNotFound : GetCapacityPolicyResult
  data object NotConfigured : GetCapacityPolicyResult
}

class GetCapacityPolicyUseCase(
  private val repository: RestaurantRepository,
) : UseCase<GetCapacityPolicyCommand, GetCapacityPolicyResult> {

  override suspend operator fun invoke(command: GetCapacityPolicyCommand): GetCapacityPolicyResult =
    runCatching { RestaurantId.of(command.restaurantId) }.fold(
      onSuccess = { policyOf(it) },
      onFailure = { GetCapacityPolicyResult.InvalidRestaurant(it.message.orEmpty()) },
    )

  private suspend fun policyOf(restaurantId: RestaurantId): GetCapacityPolicyResult {
    val restaurant = repository.findById(restaurantId)
    val policy = restaurant?.capacityPolicy
    return when {
      restaurant == null -> GetCapacityPolicyResult.RestaurantNotFound
      policy == null -> GetCapacityPolicyResult.NotConfigured
      else -> GetCapacityPolicyResult.Success(policy)
    }
  }
}

// --- Update Capacity Policy ---

data class UpdateCapacityPolicyCommand(
  val managerId: String,
  val restaurantId: String,
  val slotMinutes: Int,
  val maxCapacityUnits: Int,
  val opening: String,
  val closing: String,
  val prepLeadMinutes: Int,
  val maxAdvanceDays: Int,
  val maxTripsPerSlot: Int,
  val maxTripKmBudget: Double,
  val maxDetourKm: Double,
)

sealed interface UpdateCapacityPolicyResult {
  data class Success(val policy: CapacityPolicy) : UpdateCapacityPolicyResult
  data class InvalidRestaurant(val error: String) : UpdateCapacityPolicyResult
  data class InvalidPolicy(val error: String) : UpdateCapacityPolicyResult
  data object NotFound : UpdateCapacityPolicyResult
  data object Unauthorized : UpdateCapacityPolicyResult
}

class UpdateCapacityPolicyUseCase(
  private val repository: RestaurantRepository,
) : UseCase<UpdateCapacityPolicyCommand, UpdateCapacityPolicyResult> {

  // Inputs are validated in this order: restaurant id, caller, policy values; the first invalid
  // one decides the result, before the repository is touched.
  override suspend operator fun invoke(
    command: UpdateCapacityPolicyCommand,
  ): UpdateCapacityPolicyResult {
    val restaurantId = runCatching { RestaurantId.of(command.restaurantId) }
    val managerId = runCatching { UserId.of(command.managerId) }.getOrNull()
    val policy = runCatching { command.toPolicy() }
    return when {
      restaurantId.isFailure ->
        UpdateCapacityPolicyResult.InvalidRestaurant(
          restaurantId.exceptionOrNull()?.message.orEmpty(),
        )
      managerId == null -> UpdateCapacityPolicyResult.Unauthorized
      policy.isFailure ->
        UpdateCapacityPolicyResult.InvalidPolicy(policy.exceptionOrNull()?.message.orEmpty())
      else -> update(restaurantId.getOrThrow(), managerId, policy.getOrThrow())
    }
  }

  private suspend fun update(
    restaurantId: RestaurantId,
    managerId: UserId,
    policy: CapacityPolicy,
  ): UpdateCapacityPolicyResult {
    val restaurant = repository.findById(restaurantId)
    return when {
      restaurant == null -> UpdateCapacityPolicyResult.NotFound
      restaurant.managerId != managerId -> UpdateCapacityPolicyResult.Unauthorized
      else -> {
        restaurant.updateCapacityPolicy(policy)
        repository.update(restaurant)
        UpdateCapacityPolicyResult.Success(policy)
      }
    }
  }

  private fun UpdateCapacityPolicyCommand.toPolicy(): CapacityPolicy = CapacityPolicy(
    slotMinutes = slotMinutes,
    maxCapacityUnits = maxCapacityUnits,
    opening = LocalTime.parse(opening),
    closing = LocalTime.parse(closing),
    prepLeadMinutes = prepLeadMinutes,
    maxAdvanceDays = maxAdvanceDays,
    maxTripsPerSlot = maxTripsPerSlot,
    maxTripKmBudget = maxTripKmBudget,
    maxDetourKm = maxDetourKm,
  )
}
