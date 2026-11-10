package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.aggregate.RestaurantDetails
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.CapacityPolicy
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.GeoPoint
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.CapacityPolicyData
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.GeoPointData
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.RestaurantDocument
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val HH_MM: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

object RestaurantDocumentFactory {

  fun Restaurant.toDocument(): RestaurantDocument = RestaurantDocument(
    id = id.value,
    managerId = managerId.value,
    name = details.name.value,
    address = details.address.value,
    phone = details.phone.value,
    email = details.email.value,
    createdAt = createdAt,
    updatedAt = updatedAt,
    capacityPolicy = capacityPolicy?.toData(),
    location = location?.toData(),
  )

  fun RestaurantDocument.toDomain(): Restaurant = Restaurant.fromDatabase(
    id = RestaurantId(id),
    managerId = UserId(managerId),
    details = RestaurantDetails(
      name = RestaurantName.of(name),
      address = Address.of(address),
      phone = Phone.of(phone),
      email = Email.of(email),
    ),
    createdAt = createdAt,
    updatedAt = updatedAt,
    capacityPolicy = capacityPolicy?.toDomain(),
    location = location?.toDomain(),
  )

  private fun CapacityPolicy.toData(): CapacityPolicyData = CapacityPolicyData(
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

  private fun CapacityPolicyData.toDomain(): CapacityPolicy = CapacityPolicy(
    slotMinutes = slotMinutes,
    maxCapacityUnits = maxCapacityUnits,
    opening = LocalTime.parse(opening, HH_MM),
    closing = LocalTime.parse(closing, HH_MM),
    prepLeadMinutes = prepLeadMinutes,
    maxAdvanceDays = maxAdvanceDays,
    maxTripsPerSlot = maxTripsPerSlot,
    maxTripKmBudget = maxTripKmBudget,
    maxDetourKm = maxDetourKm,
  )

  private fun GeoPoint.toData(): GeoPointData = GeoPointData(lat = lat, lng = lng)

  private fun GeoPointData.toDomain(): GeoPoint = GeoPoint(lat = lat, lng = lng)
}
