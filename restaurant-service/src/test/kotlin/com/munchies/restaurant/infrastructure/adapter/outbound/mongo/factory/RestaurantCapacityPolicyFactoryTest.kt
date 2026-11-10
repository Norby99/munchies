package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.CapacityPolicy
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory.RestaurantDocumentFactory.toDocument
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory.RestaurantDocumentFactory.toDomain
import java.time.LocalTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RestaurantCapacityPolicyFactoryTest {

  private fun restaurant(policy: CapacityPolicy?): Restaurant {
    val base = Restaurant.create(
      managerId = UserId.of("manager-1"),
      name = RestaurantName.of("Trattoria"),
      address = Address.of("Via Roma 1"),
      phone = Phone.of("+39 06 1234567"),
      email = Email.of("info@trattoria.com"),
    )
    if (policy != null) base.updateCapacityPolicy(policy)
    return base
  }

  @Test
  fun `round-trips a configured capacity policy through the document`() {
    val policy = CapacityPolicy(
      slotMinutes = 30,
      maxCapacityUnits = 20,
      opening = LocalTime.of(11, 0),
      closing = LocalTime.of(22, 30),
      prepLeadMinutes = 30,
      maxAdvanceDays = 7,
      maxTripsPerSlot = 2,
      maxTripKmBudget = 12.5,
      maxDetourKm = 0.75,
    )
    val original = restaurant(policy)

    val roundTripped = original.toDocument().toDomain()

    assertEquals(policy, roundTripped.capacityPolicy)
    assertEquals(original.id, roundTripped.id)
  }

  @Test
  fun `round-trips a restaurant without a capacity policy as null`() {
    val roundTripped = restaurant(null).toDocument().toDomain()

    assertNull(roundTripped.capacityPolicy)
  }

  @Test
  fun `stores times as HH mm strings in the document`() {
    val policy = CapacityPolicy(
      slotMinutes = 30,
      maxCapacityUnits = 5,
      opening = LocalTime.of(9, 5),
      closing = LocalTime.of(21, 0),
      prepLeadMinutes = 0,
      maxAdvanceDays = 1,
      maxTripsPerSlot = 1,
      maxTripKmBudget = 1.0,
      maxDetourKm = 0.0,
    )

    val data = restaurant(policy).toDocument().capacityPolicy

    assertEquals("09:05", data!!.opening)
    assertEquals("21:00", data.closing)
  }
}
