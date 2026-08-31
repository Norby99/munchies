package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.MenuName
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.GetRestaurantMenusResponse
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository.MongoCrudMenuRepository
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository.MongoMenuRepository
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

@MicronautTest(environments = ["prod"], transactional = false)
class GetRestaurantMenusControllerComponentTest : BaseMenuController() {

  @Inject
  lateinit var menuRepository: MongoMenuRepository

  @Inject
  lateinit var mongoCrudMenuRepository: MongoCrudMenuRepository

  @AfterEach
  fun cleanupMongo() {
    mongoCrudMenuRepository.deleteAll()
  }

  private fun seedMenu(restaurantId: RestaurantId, name: String): Menu {
    val menu = Menu.create(restaurantId, MenuName.of(name))
    menuRepository.save(menu)
    return menu
  }

  @Test
  fun `getRestaurantMenus should return 200 OK with only that restaurant's menu summaries`() {
    val restaurantId = RestaurantId()
    seedMenu(restaurantId, "Lunch")
    seedMenu(restaurantId, "Dinner")
    seedMenu(RestaurantId(), "Someone else's menu")

    val response =
      httpGet<GetRestaurantMenusResponse>("/restaurant/${restaurantId.value}/menus")

    response.status shouldBe HttpStatus.OK
    val names = response.body()!!.result.map { it.name }
    names shouldHaveSize 2
    names.toSet() shouldBe setOf("Lunch", "Dinner")
  }

  @Test
  fun `getRestaurantMenus should return 200 OK with empty list when restaurant has no menus`() {
    val response =
      httpGet<GetRestaurantMenusResponse>("/restaurant/${RestaurantId().value}/menus")

    response.status shouldBe HttpStatus.OK
    response.body()!!.result.toList() shouldHaveSize 0
  }
}
