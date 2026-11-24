package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.CookStation
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.KitchenRoute
import com.munchies.restaurant.domain.valueobject.restaurant.KitchenSetup
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory.RestaurantDocumentFactory.toDocument
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory.RestaurantDocumentFactory.toDomain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RestaurantKitchenSetupFactoryTest {

  private fun restaurant(): Restaurant = Restaurant.create(
    managerId = UserId.of("manager-1"),
    name = RestaurantName.of("Trattoria"),
    address = Address.of("Via Roma 1"),
    phone = Phone.of("+39 06 1234567"),
    email = Email.of("info@trattoria.com"),
  )

  @Test
  fun `round-trips the kitchen setup through the document, keeping the station order`() {
    val setup = KitchenSetup(
      route = KitchenRoute.COOK_THEN_PREP,
      prepWorkers = 3,
      cookStations = listOf(CookStation("Grill", 4), CookStation("Fryer", 2)),
      prepMinutes = 2.5,
      loadingMinutes = 0.25,
      cookMinutes = 6.0,
    )
    val original = restaurant().apply { updateKitchenSetup(setup) }

    val document = original.toDocument()

    assertEquals("COOK_THEN_PREP", document.kitchenSetup?.route)
    assertEquals(setup, document.toDomain().kitchenSetup)
  }

  @Test
  fun `a restaurant without a kitchen setup round-trips with none`() {
    assertNull(restaurant().toDocument().toDomain().kitchenSetup)
  }
}
