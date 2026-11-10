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

  override suspend operator fun invoke(command: GetCapacityPolicyCommand): GetCapacityPolicyResult {
    val restaurantId = runCatching { RestaurantId.of(command.restaurantId) }
      .getOrElse { return GetCapacityPolicyResult.InvalidRestaurant(it.message.orEmpty()) }
    val restaurant = repository.findById(restaurantId)
      ?: return GetCapacityPolicyResult.RestaurantNotFound
    val policy = restaurant.capacityPolicy
      ?: return GetCapacityPolicyResult.NotConfigured
    return GetCapacityPolicyResult.Success(policy)
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

  override suspend operator fun invoke(
    command: UpdateCapacityPolicyCommand,
  ): UpdateCapacityPolicyResult {
    val restaurantId = runCatching { RestaurantId.of(command.restaurantId) }
      .getOrElse { return UpdateCapacityPolicyResult.InvalidRestaurant(it.message.orEmpty()) }
    val managerId = runCatching { UserId.of(command.managerId) }
      .getOrElse { return UpdateCapacityPolicyResult.Unauthorized }
    val policy = runCatching { command.toPolicy() }
      .getOrElse { return UpdateCapacityPolicyResult.InvalidPolicy(it.message.orEmpty()) }

    val restaurant = repository.findById(restaurantId)
      ?: return UpdateCapacityPolicyResult.NotFound
    if (restaurant.managerId != managerId) return UpdateCapacityPolicyResult.Unauthorized

    restaurant.updateCapacityPolicy(policy)
    repository.update(restaurant)
    return UpdateCapacityPolicyResult.Success(policy)
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
