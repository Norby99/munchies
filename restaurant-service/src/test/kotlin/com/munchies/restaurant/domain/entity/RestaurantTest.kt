package com.munchies.restaurant.domain.entity

import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RestaurantTest {

  private fun createValidRestaurant(): Restaurant {
    return Restaurant.create(
      managerId = UserId("manager-123"),
      details = RestaurantDetails(
        name = RestaurantName.of("Test Restaurant"),
        address = Address.of("123 Main Street"),
        phone = Phone.of("123456789"),
        email = Email.of("test@restaurant.com"),
      ),
    )
  }

  @Test
  fun `should create restaurant with valid data using factory method`() {
    val restaurant = createValidRestaurant()

    assertNotNull(restaurant.id)
    assertEquals("Test Restaurant", restaurant.details.name.value)
    assertEquals("123 Main Street", restaurant.details.address.value)
    assertEquals("123456789", restaurant.details.phone.value)
    assertEquals("test@restaurant.com", restaurant.details.email.value)
    assertEquals("manager-123", restaurant.managerId.value)
    assertNotNull(restaurant.createdAt)
    assertNotNull(restaurant.updatedAt)
    assertEquals(restaurant.createdAt, restaurant.updatedAt)
  }

  @Test
  fun `should generate unique IDs for different restaurants`() {
    val restaurant1 = createValidRestaurant()
    val restaurant2 = createValidRestaurant()

    assertNotNull(restaurant1.id)
    assertNotNull(restaurant2.id)
    assertTrue(restaurant1.id.value != restaurant2.id.value)
  }

  @Test
  fun `should set createdAt and updatedAt to same timestamp on creation`() {
    val restaurant = createValidRestaurant()

    assertEquals(restaurant.createdAt, restaurant.updatedAt)
  }

  @Test
  fun `should recreate restaurant from database with all properties preserved`() {
    val restaurantId = RestaurantId.of("test-id-123")
    val restaurantName = RestaurantName.of("Restored Restaurant")
    val address = Address.of("456 Oak Avenue")
    val phone = Phone.of("987654321")
    val email = Email.of("restored@restaurant.com")
    val managerId = UserId.of("manager-456")
    val createdAt = LocalDateTime.now().minusDays(7)
    val updatedAt = LocalDateTime.now().minusHours(2)

    val restaurant = Restaurant.fromDatabase(
      id = restaurantId,
      managerId = managerId,
      details = RestaurantDetails(
        name = restaurantName,
        address = address,
        phone = phone,
        email = email,
      ),
      createdAt = createdAt,
      updatedAt = updatedAt,
    )

    assertEquals(restaurantId.value, restaurant.id.value)
    assertEquals("Restored Restaurant", restaurant.details.name.value)
    assertEquals("456 Oak Avenue", restaurant.details.address.value)
    assertEquals("987654321", restaurant.details.phone.value)
    assertEquals("restored@restaurant.com", restaurant.details.email.value)
    assertEquals("manager-456", restaurant.managerId.value)
    assertEquals(createdAt, restaurant.createdAt)
    assertEquals(updatedAt, restaurant.updatedAt)
  }

  @Test
  fun `should update only name when provided`() {
    val restaurant = createValidRestaurant()
    val originalAddress = restaurant.details.address
    val originalPhone = restaurant.details.phone
    val originalEmail = restaurant.details.email
    val originalUpdatedAt = restaurant.updatedAt
    val newName = RestaurantName.of("Updated Name")

    Thread.sleep(10)
    restaurant.updateName(newName)

    assertEquals("Updated Name", restaurant.details.name.value)
    assertEquals(originalAddress.value, restaurant.details.address.value)
    assertEquals(originalPhone.value, restaurant.details.phone.value)
    assertEquals(originalEmail.value, restaurant.details.email.value)
    assertTrue(restaurant.updatedAt.isAfter(originalUpdatedAt))
  }

  @Test
  fun `should update only address when provided`() {
    val restaurant = createValidRestaurant()
    val originalName = restaurant.details.name
    val originalPhone = restaurant.details.phone
    val originalEmail = restaurant.details.email
    val originalUpdatedAt = restaurant.updatedAt
    val newAddress = Address.of("999 New Street")

    Thread.sleep(10)
    restaurant.updateAddress(newAddress)

    assertEquals(originalName.value, restaurant.details.name.value)
    assertEquals("999 New Street", restaurant.details.address.value)
    assertEquals(originalPhone.value, restaurant.details.phone.value)
    assertEquals(originalEmail.value, restaurant.details.email.value)
    assertTrue(restaurant.updatedAt.isAfter(originalUpdatedAt))
  }

  @Test
  fun `should update only phone when provided`() {
    val restaurant = createValidRestaurant()
    val originalName = restaurant.details.name
    val originalAddress = restaurant.details.address
    val originalEmail = restaurant.details.email
    val originalUpdatedAt = restaurant.updatedAt
    val newPhone = Phone.of("555666777")

    Thread.sleep(10)
    restaurant.updatePhone(newPhone)

    assertEquals(originalName.value, restaurant.details.name.value)
    assertEquals(originalAddress.value, restaurant.details.address.value)
    assertEquals("555666777", restaurant.details.phone.value)
    assertEquals(originalEmail.value, restaurant.details.email.value)
    assertTrue(restaurant.updatedAt.isAfter(originalUpdatedAt))
  }

  @Test
  fun `should update only email when provided`() {
    val restaurant = createValidRestaurant()
    val originalName = restaurant.details.name
    val originalAddress = restaurant.details.address
    val originalPhone = restaurant.details.phone
    val originalUpdatedAt = restaurant.updatedAt
    val newEmail = Email.of("newemail@restaurant.com")

    Thread.sleep(10)
    restaurant.updateEmail(newEmail)

    assertEquals(originalName.value, restaurant.details.name.value)
    assertEquals(originalAddress.value, restaurant.details.address.value)
    assertEquals(originalPhone.value, restaurant.details.phone.value)
    assertEquals("newemail@restaurant.com", restaurant.details.email.value)
    assertTrue(restaurant.updatedAt.isAfter(originalUpdatedAt))
  }

  @Test
  fun `should update multiple properties at once`() {
    val restaurant = createValidRestaurant()
    val originalUpdatedAt = restaurant.updatedAt
    val newDetails = RestaurantDetails(
      name = RestaurantName.of("Multi Updated"),
      address = Address.of("777 Multi Street"),
      phone = Phone.of("111222333"),
      email = Email.of("multi@restaurant.com"),
    )

    Thread.sleep(10)
    restaurant.updateDetails(newDetails)

    assertEquals("Multi Updated", restaurant.details.name.value)
    assertEquals("777 Multi Street", restaurant.details.address.value)
    assertEquals("111222333", restaurant.details.phone.value)
    assertEquals("multi@restaurant.com", restaurant.details.email.value)
    assertTrue(restaurant.updatedAt.isAfter(originalUpdatedAt))
  }

  @Test
  fun `should preserve createdAt timestamp across updates`() {
    val restaurant = createValidRestaurant()
    val originalCreatedAt = restaurant.createdAt

    Thread.sleep(10)
    restaurant.updateName(RestaurantName.of("New Name"))

    assertEquals(originalCreatedAt, restaurant.createdAt)
  }

  @Test
  fun `should have AggregateRoot identity`() {
    val restaurant = createValidRestaurant()

    assertEquals(restaurant.id.value, restaurant.id.value)
    assertNotNull(restaurant.id)
  }

  @Test
  fun `should allow multiple sequential updates`() {
    val restaurant = createValidRestaurant()

    Thread.sleep(10)
    restaurant.updateName(RestaurantName.of("First Update"))
    val firstUpdateTime = restaurant.updatedAt

    Thread.sleep(10)
    restaurant.updateAddress(Address.of("Second Update Street"))
    val secondUpdateTime = restaurant.updatedAt

    Thread.sleep(10)
    restaurant.updatePhone(Phone.of("123123123"))

    assertEquals("First Update", restaurant.details.name.value)
    assertEquals("Second Update Street", restaurant.details.address.value)
    assertEquals("123123123", restaurant.details.phone.value)
    assertTrue(secondUpdateTime.isAfter(firstUpdateTime))
    assertTrue(restaurant.updatedAt.isAfter(secondUpdateTime))
  }

  @Test
  fun `should handle updating all properties individually in sequence`() {
    val restaurant = createValidRestaurant()

    restaurant.updateName(RestaurantName.of("Name1"))
    assertEquals("Name1", restaurant.details.name.value)

    Thread.sleep(10)
    restaurant.updateAddress(Address.of("Address1"))
    assertEquals("Address1", restaurant.details.address.value)

    Thread.sleep(10)
    restaurant.updatePhone(Phone.of("111111111"))
    assertEquals("111111111", restaurant.details.phone.value)

    Thread.sleep(10)
    restaurant.updateEmail(Email.of("email1@restaurant.com"))
    assertEquals("email1@restaurant.com", restaurant.details.email.value)
  }

  @Test
  fun `should persist createdAt even after multiple updates`() {
    val restaurant = createValidRestaurant()
    val originalCreatedAt = restaurant.createdAt

    Thread.sleep(10)
    restaurant.updateName(RestaurantName.of("Update 1"))
    Thread.sleep(10)
    restaurant.updateAddress(Address.of("Update 2"))
    Thread.sleep(10)
    restaurant.updatePhone(Phone.of("222222222"))

    assertEquals(originalCreatedAt, restaurant.createdAt)
    assertTrue(restaurant.updatedAt.isAfter(originalCreatedAt))
  }

  @Test
  fun `should have unique restaurant ID for each created restaurant`() {
    val restaurant1 = Restaurant.create(
      managerId = UserId("manager-1"),
      details = RestaurantDetails(
        name = RestaurantName.of("Restaurant 1"),
        address = Address.of("Address 1"),
        phone = Phone.of("111111111"),
        email = Email.of("rest1@test.com"),
      ),
    )
    val restaurant2 = Restaurant.create(
      managerId = UserId("manager-2"),
      details = RestaurantDetails(
        name = RestaurantName.of("Restaurant 2"),
        address = Address.of("Address 2"),
        phone = Phone.of("222222222"),
        email = Email.of("rest2@test.com"),
      ),
    )

    assertTrue(restaurant1.id.value != restaurant2.id.value)
  }

  @Test
  fun `should maintain immutable manager ID after creation`() {
    val expectedManagerId = UserId("manager-xyz")
    val restaurant = Restaurant.create(
      managerId = expectedManagerId,
      details = RestaurantDetails(
        name = RestaurantName.of("Test"),
        address = Address.of("Test Address"),
        phone = Phone.of("123456789"),
        email = Email.of("test@test.com"),
      ),
    )

    assertEquals(expectedManagerId.value, restaurant.managerId.value)
  }
}
