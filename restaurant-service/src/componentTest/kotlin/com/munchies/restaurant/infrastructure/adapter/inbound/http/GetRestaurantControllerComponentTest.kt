package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetRestaurantResponse
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
class GetRestaurantControllerComponentTest : BaseMenuController() {

  @Inject
  lateinit var restaurantRepository: MongoRestaurantRepository

  @Inject
  lateinit var mongoCrudRestaurantRepository: MongoCrudRestaurantRepository

  @AfterEach
  fun cleanupMongo() {
    mongoCrudRestaurantRepository.deleteAll()
  }

  private fun seedRestaurant(): Restaurant {
    val restaurant = Restaurant.create(
      managerId = UserId(),
      name = RestaurantName.of("Trattoria da Piero"),
      address = Address.of("Via Roma 1, Milano"),
      phone = Phone.of("+39 333 1234567"),
      email = Email.of("piero@example.com"),
    )
    restaurantRepository.save(restaurant)
    return restaurant
  }

  @Test
  fun `getRestaurant should return 200 OK with the restaurant details`() {
    val restaurant = seedRestaurant()

    val response = httpGet<GetRestaurantResponse>("/restaurants/${restaurant.id.value}/")

    response.status shouldBe HttpStatus.OK
    response.body()!!.result.name shouldBe "Trattoria da Piero"
    response.body()!!.result.address shouldBe "Via Roma 1, Milano"
  }

  @Test
  fun `getRestaurant should return 404 Not Found when the restaurant does not exist`() {
    val ex = assertThrows<HttpClientResponseException> {
      httpGet<GetRestaurantResponse>("/restaurants/${RestaurantId().value}/")
    }

    ex.response.status shouldBe HttpStatus.NOT_FOUND
  }
}
