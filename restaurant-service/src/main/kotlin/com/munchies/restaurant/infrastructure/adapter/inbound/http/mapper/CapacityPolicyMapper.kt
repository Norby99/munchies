package com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper

import com.munchies.restaurant.application.usecase.restaurant.GetCapacityPolicyResult
import com.munchies.restaurant.application.usecase.restaurant.UpdateCapacityPolicyCommand
import com.munchies.restaurant.application.usecase.restaurant.UpdateCapacityPolicyResult
import com.munchies.restaurant.domain.valueobject.restaurant.CapacityPolicy
import com.munchies.restaurant.infrastructure.adapter.dto.CapacityPolicyDto
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetCapacityPolicyResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateCapacityPolicyRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateCapacityPolicyResponse
import java.time.format.DateTimeFormatter

private val HH_MM: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun CapacityPolicy.toDto(): CapacityPolicyDto = CapacityPolicyDto(
  slotMinutes = slotMinutes,
  maxCapacityUnits = maxCapacityUnits,
  opening = opening.format(HH_MM),
  closing = closing.format(HH_MM),
  prepLeadMinutes = prepLeadMinutes,
  maxAdvanceDays = maxAdvanceDays,
  maxTripsPerSlot = maxTripsPerSlot,
  maxTripKmBudget = maxTripKmBudget,
  maxDetourKm = maxDetourKm,
)

fun GetCapacityPolicyResult.Success.toResponse(): GetCapacityPolicyResponse =
  GetCapacityPolicyResponse(policy.toDto())

fun UpdateCapacityPolicyRequest.toCommand(restaurantId: String): UpdateCapacityPolicyCommand =
  UpdateCapacityPolicyCommand(
    managerId = managerId,
    restaurantId = restaurantId,
    slotMinutes = slotMinutes,
    maxCapacityUnits = maxCapacityUnits,
    opening = opening,
    closing = closing,
    prepLeadMinutes = prepLeadMinutes,
    maxAdvanceDays = maxAdvanceDays,
    maxTripsPerSlot = maxTripsPerSlot,
    maxTripKmBudget = maxTripKmBudget,
    maxDetourKm = maxDetourKm,
  )

fun UpdateCapacityPolicyResult.Success.toResponse(): UpdateCapacityPolicyResponse =
  UpdateCapacityPolicyResponse(policy.toDto())
