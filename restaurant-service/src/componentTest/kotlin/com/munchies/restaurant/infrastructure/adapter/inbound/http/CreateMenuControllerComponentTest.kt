package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.CreateMenuRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.CreateMenuResponse
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository.MongoCrudMenuRepository
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository.MongoCrudRestaurantRepository
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository.MongoMenuRepository
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
class CreateMenuControllerComponentTest : BaseMenuController() {

  @Inject
  lateinit var menuRepository: MongoMenuRepository

  @Inject
  lateinit var mongoCrudMenuRepository: MongoCrudMenuRepository

  @Inject
  lateinit var restaurantRepository: MongoRestaurantRepository

  @Inject
  lateinit var mongoCrudRestaurantRepository: MongoCrudRestaurantRepository

  @AfterEach
  fun cleanupMongo() {
    mongoCrudMenuRepository.deleteAll()
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
  fun `createMenu should return 201 Created and persist an always-valid menu`() {
    val restaurant = seedRestaurant()

    val response = httpPost<CreateMenuResponse>(
      "/restaurant/${restaurant.id.value}/menus",
      CreateMenuRequest(managerId = restaurant.managerId.value, name = "Lunch"),
    )

    response.status shouldBe HttpStatus.CREATED
    val menuId = response.body()!!.result.id

    val persisted = menuRepository.findByIdAndRestaurantId(MenuId(menuId), restaurant.id)!!
    persisted.name.value shouldBe "Lunch"
  }

  @Test
  fun `createMenu should return 404 Not Found when the restaurant does not exist`() {
    val ex = assertThrows<HttpClientResponseException> {
      httpPost<CreateMenuResponse>(
        "/restaurant/${RestaurantId().value}/menus",
        CreateMenuRequest(managerId = UserId().value, name = "Lunch"),
      )
    }

    ex.response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `createMenu should return 401 Unauthorized when caller is not the owning manager`() {
    val restaurant = seedRestaurant()

    val ex = assertThrows<HttpClientResponseException> {
      httpPost<CreateMenuResponse>(
        "/restaurant/${restaurant.id.value}/menus",
        CreateMenuRequest(managerId = UserId().value, name = "Lunch"),
      )
    }

    ex.response.status shouldBe HttpStatus.UNAUTHORIZED
    ex.response.bd<ErrorResponse>().result shouldBe "Unauthorized to create menu"
  }

  @Test
  fun `createMenu should return 422 Unprocessable Entity when the name is blank`() {
    val restaurant = seedRestaurant()

    val ex = assertThrows<HttpClientResponseException> {
      httpPost<CreateMenuResponse>(
        "/restaurant/${restaurant.id.value}/menus",
        CreateMenuRequest(managerId = restaurant.managerId.value, name = "   "),
      )
    }

    ex.response.status shouldBe HttpStatus.UNPROCESSABLE_ENTITY
  }
}
