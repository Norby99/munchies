package com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RestaurantDomainMapperTest {
  @Test
  fun `should map every restaurant field onto the dto`() {
    val restaurant = Restaurant.create(
      managerId = UserId.of("manager-1"),
      name = RestaurantName.of("Trattoria Roma"),
      address = Address.of("Via Roma 1"),
      phone = Phone.of("+39 06 1234567"),
      email = Email.of("info@trattoria-roma.com"),
    )

    val dto = restaurant.toDto()

    assertEquals(restaurant.id.value, dto.id)
    assertEquals("Trattoria Roma", dto.name)
    assertEquals("Via Roma 1", dto.address)
    assertEquals("+39 06 1234567", dto.phone)
    assertEquals("info@trattoria-roma.com", dto.email)
  }
}
