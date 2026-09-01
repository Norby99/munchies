package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.menu.MenuName
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import com.munchies.restaurant.infrastructure.adapter.dto.ValidityDto
import com.munchies.restaurant.infrastructure.adapter.dto.ValidityType
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.UpdateMenuRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.UpdateMenuResponse
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
class UpdateMenuControllerComponentTest : BaseMenuController() {

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

  private fun seedRestaurantAndMenu(managerId: UserId = UserId()): Pair<Restaurant, Menu> {
    val restaurant = Restaurant.create(
      managerId = managerId,
      name = RestaurantName.of("Trattoria da Piero"),
      address = Address.of("Via Roma 1, Milano"),
      phone = Phone.of("+39 333 1234567"),
      email = Email.of("piero@example.com"),
    )
    restaurantRepository.save(restaurant)
    val menu = Menu.create(restaurant.id, MenuName.of("Lunch"))
    menuRepository.save(menu)
    return restaurant to menu
  }

  private fun request(managerId: String, name: String = "Weekday Lunch") = UpdateMenuRequest(
    managerId = managerId,
    name = name,
    validity = arrayOf(ValidityDto(ValidityType.ALWAYS)),
  )

  @Test
  fun `updateMenu should return 200 OK and persist the new name`() {
    val (restaurant, menu) = seedRestaurantAndMenu()

    val response = httpPut<UpdateMenuResponse>(
      "/restaurant/${restaurant.id.value}/menus/${menu.id.value}",
      request(restaurant.managerId.value),
    )

    response.status shouldBe HttpStatus.OK
    response.body()!!.result.name shouldBe "Weekday Lunch"
    menuRepository.findById(menu.id)!!.name.value shouldBe "Weekday Lunch"
  }

  @Test
  fun `updateMenu should return 404 Not Found when the menu does not exist`() {
    val restaurant = Restaurant.create(
      managerId = UserId(),
      name = RestaurantName.of("Trattoria da Piero"),
      address = Address.of("Via Roma 1, Milano"),
      phone = Phone.of("+39 333 1234567"),
      email = Email.of("piero@example.com"),
    )
    restaurantRepository.save(restaurant)

    val ex = assertThrows<HttpClientResponseException> {
      httpPut<UpdateMenuResponse>(
        "/restaurant/${restaurant.id.value}/menus/${MenuId().value}",
        request(restaurant.managerId.value),
      )
    }

    ex.response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `updateMenu should return 401 Unauthorized when the caller is not the owning manager`() {
    val (restaurant, menu) = seedRestaurantAndMenu()

    val ex = assertThrows<HttpClientResponseException> {
      httpPut<UpdateMenuResponse>(
        "/restaurant/${restaurant.id.value}/menus/${menu.id.value}",
        request(UserId().value),
      )
    }

    ex.response.status shouldBe HttpStatus.UNAUTHORIZED
    ex.response.bd<ErrorResponse>().result shouldBe "Unauthorized to update menu"
  }
}
