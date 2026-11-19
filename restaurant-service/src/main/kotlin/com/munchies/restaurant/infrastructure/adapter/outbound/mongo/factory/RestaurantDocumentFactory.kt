package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.aggregate.RestaurantDetails
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.CapacityPolicy
import com.munchies.restaurant.domain.valueobject.restaurant.CookStation
import com.munchies.restaurant.domain.valueobject.restaurant.DeliveryZone
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.GeoPoint
import com.munchies.restaurant.domain.valueobject.restaurant.KitchenRoute
import com.munchies.restaurant.domain.valueobject.restaurant.KitchenSetup
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import com.munchies.restaurant.domain.valueobject.restaurant.Table
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.CapacityPolicyData
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.CookStationData
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.DeliveryZoneData
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.GeoPointData
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.KitchenSetupData
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.RestaurantDocument
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.TableData
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
    tables = tables.map { TableData(number = it.number, seats = it.seats) },
    deliveryZones = deliveryZones.map { it.toData() },
    kitchenSetup = kitchenSetup?.toData(),
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
    tables = tables.map { Table(number = it.number, seats = it.seats) },
    deliveryZones = deliveryZones.map { it.toDomain() },
    kitchenSetup = kitchenSetup?.toDomain(),
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

  private fun DeliveryZone.toData(): DeliveryZoneData = DeliveryZoneData(
    id = id,
    name = name,
    boundary = boundary.map { it.toData() },
  )

  private fun DeliveryZoneData.toDomain(): DeliveryZone = DeliveryZone(
    id = id,
    name = name,
    boundary = boundary.map { it.toDomain() },
  )

  private fun KitchenSetup.toData(): KitchenSetupData = KitchenSetupData(
    route = route.name,
    prepWorkers = prepWorkers,
    cookStations = cookStations.map { CookStationData(name = it.name, places = it.places) },
    prepMinutes = prepMinutes,
    loadingMinutes = loadingMinutes,
    cookMinutes = cookMinutes,
  )

  private fun KitchenSetupData.toDomain(): KitchenSetup = KitchenSetup(
    route = KitchenRoute.valueOf(route),
    prepWorkers = prepWorkers,
    cookStations = cookStations.map { CookStation(name = it.name, places = it.places) },
    prepMinutes = prepMinutes,
    loadingMinutes = loadingMinutes,
    cookMinutes = cookMinutes,
  )
}
