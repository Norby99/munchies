package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.DeleteRestaurantRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.DeleteRestaurantResponse
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository.MongoCrudRestaurantRepository
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository.MongoRestaurantRepository
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@MicronautTest(environments = ["prod"], transactional = false)
class DeleteRestaurantControllerComponentTest : BaseMenuController() {

  @Inject
  lateinit var restaurantRepository: MongoRestaurantRepository

  @Inject
  lateinit var mongoCrudRestaurantRepository: MongoCrudRestaurantRepository

  @AfterEach
  fun cleanupMongo() {
    mongoCrudRestaurantRepository.deleteAll()
  }

  private fun seedRestaurant(managerId: UserId = UserId()): Restaurant {
    val restaurant = Restaurant.create(
      managerId = managerId,
      name = RestaurantName.of("Trattoria da Piero"),
      address = Address.of("Via Roma 1, Milano"),
      phone = Phone.of("+39 333 1234567"),
      email = Email.of("piero@example.com"),
    )
    restaurantRepository.save(restaurant)
    return restaurant
  }

  @Test
  fun `deleteRestaurant should return 200 OK and remove the restaurant`() {
    val managerId = UserId()
    val restaurant = seedRestaurant(managerId)

    val response = httpDelete<DeleteRestaurantResponse>(
      "/restaurants/${restaurant.id.value}",
      DeleteRestaurantRequest(managerId = managerId.value),
    )

    response.status shouldBe HttpStatus.OK
    restaurantRepository.findById(restaurant.id).shouldBeNull()
  }

  @Test
  fun `deleteRestaurant should return 404 Not Found when the restaurant does not exist`() {
    val ex = assertThrows<HttpClientResponseException> {
      httpDelete<DeleteRestaurantResponse>(
        "/restaurants/${RestaurantId().value}",
        DeleteRestaurantRequest(managerId = UserId().value),
      )
    }

    ex.response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `deleteRestaurant should return 401 Unauthorized when caller is not the owning manager`() {
    val restaurant = seedRestaurant()

    val ex = assertThrows<HttpClientResponseException> {
      httpDelete<DeleteRestaurantResponse>(
        "/restaurants/${restaurant.id.value}",
        DeleteRestaurantRequest(managerId = UserId().value),
      )
    }

    ex.response.status shouldBe HttpStatus.UNAUTHORIZED
    ex.response.bd<ErrorResponse>().result shouldBe "Unauthorized to delete restaurant"
    restaurantRepository.findById(restaurant.id) shouldBe restaurant
  }
}
