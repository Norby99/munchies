package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import com.munchies.restaurant.domain.valueobject.restaurant.Table
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory.RestaurantDocumentFactory.toDocument
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory.RestaurantDocumentFactory.toDomain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RestaurantTablesFactoryTest {

  private fun restaurant(): Restaurant = Restaurant.create(
    managerId = UserId.of("manager-1"),
    name = RestaurantName.of("Trattoria"),
    address = Address.of("Via Roma 1"),
    phone = Phone.of("+39 06 1234567"),
    email = Email.of("info@trattoria.com"),
  )

  @Test
  fun `round-trips the configured tables through the document`() {
    val original = restaurant()
    original.updateTables(listOf(Table(1, 2), Table(2, 6)))

    val roundTripped = original.toDocument().toDomain()

    assertEquals(listOf(Table(1, 2), Table(2, 6)), roundTripped.tables)
  }

  @Test
  fun `a restaurant without tables round-trips to an empty layout`() {
    val roundTripped = restaurant().toDocument().toDomain()
    assertTrue(roundTripped.tables.isEmpty())
  }
}
