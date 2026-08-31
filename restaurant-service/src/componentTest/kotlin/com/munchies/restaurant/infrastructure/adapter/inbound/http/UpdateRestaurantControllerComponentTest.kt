package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateRestaurantRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateRestaurantResponse
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository.MongoCrudRestaurantRepository
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository.MongoRestaurantRepository
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@MicronautTest(environments = ["prod"], transactional = false)
class UpdateRestaurantControllerComponentTest : BaseMenuController() {

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

  private fun request(managerId: String, name: String = "Trattoria da Piero e Figli") =
    UpdateRestaurantRequest(
      managerId = managerId,
      name = name,
      address = "Via Milano 2, Roma",
      phone = "+39 333 7654321",
      email = "figli@example.com",
    )

  @Test
  fun `updateRestaurant should return 200 OK and persist the new details`() {
    val managerId = UserId()
    val restaurant = seedRestaurant(managerId)

    val response = httpPut<UpdateRestaurantResponse>(
      "/restaurants/${restaurant.id.value}/",
      request(managerId.value),
    )

    response.status shouldBe HttpStatus.OK
    restaurantRepository.findById(restaurant.id)!!.name.value shouldBe "Trattoria da Piero e Figli"
  }

  @Test
  fun `updateRestaurant should return 404 Not Found when the restaurant does not exist`() {
    val ex = assertThrows<HttpClientResponseException> {
      httpPut<UpdateRestaurantResponse>(
        "/restaurants/${RestaurantId().value}/",
        request(UserId().value),
      )
    }

    ex.response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `updateRestaurant should return 401 Unauthorized when caller is not the owning manager`() {
    val restaurant = seedRestaurant()

    val ex = assertThrows<HttpClientResponseException> {
      httpPut<UpdateRestaurantResponse>(
        "/restaurants/${restaurant.id.value}/",
        request(UserId().value),
      )
    }

    ex.response.status shouldBe HttpStatus.UNAUTHORIZED
    ex.response.bd<ErrorResponse>().result shouldBe "Unauthorized to update restaurant"
  }

  @Test
  fun `updateRestaurant should return 409 Conflict when renaming to a name manager already uses`() {
    val managerId = UserId()
    seedRestaurant(managerId)
    val secondRestaurant = Restaurant.create(
      managerId = managerId,
      name = RestaurantName.of("Osteria del Porto"),
      address = Address.of("Via Milano 2, Roma"),
      phone = Phone.of("+39 333 7654321"),
      email = Email.of("porto@example.com"),
    )
    restaurantRepository.save(secondRestaurant)

    val ex = assertThrows<HttpClientResponseException> {
      httpPut<UpdateRestaurantResponse>(
        "/restaurants/${secondRestaurant.id.value}/",
        request(managerId.value, name = "Trattoria da Piero"),
      )
    }

    ex.response.status shouldBe HttpStatus.CONFLICT
  }
}
