package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.MenuName
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.GetMenuResponse
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository.MongoCrudMenuRepository
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository.MongoMenuRepository
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@MicronautTest(environments = ["prod"], transactional = false)
class GetMenuControllerComponentTest : BaseMenuController() {

  @Inject
  lateinit var menuRepository: MongoMenuRepository

  @Inject
  lateinit var mongoCrudMenuRepository: MongoCrudMenuRepository

  @AfterEach
  fun cleanupMongo() {
    mongoCrudMenuRepository.deleteAll()
  }

  private fun seedMenu(): Menu {
    val menu = Menu.create(RestaurantId(), MenuName.of("Lunch"))
    menuRepository.save(menu)
    return menu
  }

  @Test
  fun `getMenu should return 200 OK with the menu`() {
    val menu = seedMenu()

    val response =
      httpGet<GetMenuResponse>("/restaurant/${menu.restaurantId.value}/menus/${menu.id.value}")

    response.status shouldBe HttpStatus.OK
    response.body()!!.result.name shouldBe "Lunch"
  }

  @Test
  fun `getMenu should return 404 Not Found when the menu does not exist`() {
    val ex = assertThrows<HttpClientResponseException> {
      httpGet<GetMenuResponse>(
        "/restaurant/${RestaurantId().value}/menus/${MenuId().value}",
      )
    }

    ex.response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `getMenu should return 404 Not Found when the menu belongs to another restaurant`() {
    val menu = seedMenu()

    val ex = assertThrows<HttpClientResponseException> {
      httpGet<GetMenuResponse>("/restaurant/${RestaurantId().value}/menus/${menu.id.value}")
    }

    ex.response.status shouldBe HttpStatus.NOT_FOUND
  }
}
