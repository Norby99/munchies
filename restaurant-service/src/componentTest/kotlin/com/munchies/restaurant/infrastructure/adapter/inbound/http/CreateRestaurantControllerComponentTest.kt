package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.CreateRestaurantRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.CreateRestaurantResponse
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
class CreateRestaurantControllerComponentTest : BaseMenuController() {

  @Inject
  lateinit var restaurantRepository: MongoRestaurantRepository

  @Inject
  lateinit var mongoCrudRestaurantRepository: MongoCrudRestaurantRepository

  @AfterEach
  fun cleanupMongo() {
    mongoCrudRestaurantRepository.deleteAll()
  }

  private fun request(managerId: String = UserId().value, name: String = "Trattoria da Piero") =
    CreateRestaurantRequest(
      managerId = managerId,
      name = name,
      address = "Via Roma 1, Milano",
      phone = "+39 333 1234567",
      email = "piero@example.com",
    )

  @Test
  fun `createRestaurant should return 201 Created and persist the restaurant`() {
    val response = httpPost<CreateRestaurantResponse>("/restaurants/", request())

    response.status shouldBe HttpStatus.CREATED
    val restaurantId = response.body()!!.result

    restaurantRepository.findById(RestaurantId.of(restaurantId))!!.name.value shouldBe
      "Trattoria da Piero"
  }

  @Test
  fun `createRestaurant should return 422 Unprocessable Entity on an invalid email`() {
    val ex = assertThrows<HttpClientResponseException> {
      httpPost<CreateRestaurantResponse>(
        "/restaurants/",
        request().copy(email = "not-an-email"),
      )
    }

    ex.response.status shouldBe HttpStatus.UNPROCESSABLE_ENTITY
  }

  @Test
  fun `createRestaurant should return 409 Conflict on duplicate restaurant name for the manager`() {
    val managerId = UserId()
    restaurantRepository.save(
      Restaurant.create(
        managerId = managerId,
        name = RestaurantName.of("Trattoria da Piero"),
        address = Address.of("Via Roma 1, Milano"),
        phone = Phone.of("+39 333 1234567"),
        email = Email.of("piero@example.com"),
      ),
    )

    val ex = assertThrows<HttpClientResponseException> {
      httpPost<CreateRestaurantResponse>("/restaurants/", request(managerId = managerId.value))
    }

    ex.response.status shouldBe HttpStatus.CONFLICT
    ex.response.bd<ErrorResponse>().result shouldBe "Name already exists"
  }
}
