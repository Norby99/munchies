package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.DeliveryZone
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.GeoPoint
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory.RestaurantDocumentFactory.toDocument
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory.RestaurantDocumentFactory.toDomain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RestaurantDeliveryZonesFactoryTest {

  private fun restaurant(): Restaurant = Restaurant.create(
    managerId = UserId.of("manager-1"),
    name = RestaurantName.of("Trattoria"),
    address = Address.of("Via Roma 1"),
    phone = Phone.of("+39 06 1234567"),
    email = Email.of("info@trattoria.com"),
  )

  @Test
  fun `round-trips delivery zones through the document, keeping their order`() {
    val zones = listOf(
      DeliveryZone(
        "z2",
        "Mare",
        listOf(GeoPoint(44.0, 12.6), GeoPoint(44.0, 12.7), GeoPoint(44.05, 12.65)),
      ),
      DeliveryZone(
        "z1",
        "Centro",
        listOf(GeoPoint(43.9, 12.6), GeoPoint(43.9, 12.7), GeoPoint(43.95, 12.65)),
      ),
    )
    val original = restaurant().apply { updateDeliveryZones(zones) }

    val roundTripped = original.toDocument().toDomain()

    assertEquals(zones, roundTripped.deliveryZones)
  }

  @Test
  fun `a restaurant without zones round-trips with none`() {
    val roundTripped = restaurant().toDocument().toDomain()

    assertTrue(roundTripped.deliveryZones.isEmpty())
  }
}
