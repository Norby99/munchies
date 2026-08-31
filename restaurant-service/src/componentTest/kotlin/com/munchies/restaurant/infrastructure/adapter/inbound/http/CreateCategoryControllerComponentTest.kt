package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.MenuName
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.CreateCategoryRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.CreateCategoryResponse
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
class CreateCategoryControllerComponentTest : BaseMenuController() {

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

  private fun categoriesPath(restaurantId: String, menuId: String) =
    "/restaurant/$restaurantId/menus/$menuId/categories"

  @Test
  fun `createCategory should return 201 Created and persist the category under the menu`() {
    val menu = seedMenu()

    val response = httpPost<CreateCategoryResponse>(
      categoriesPath(menu.restaurantId.value, menu.id.value),
      CreateCategoryRequest(name = "Starters"),
    )

    response.status shouldBe HttpStatus.CREATED
    response.body()!!.result.name shouldBe "Starters"

    val persisted = menuRepository.findById(menu.id)!!
    persisted.categories.first().name.value shouldBe "Starters"
  }

  @Test
  fun `createCategory should return 404 Not Found when the menu does not exist`() {
    val ex = assertThrows<HttpClientResponseException> {
      httpPost<CreateCategoryResponse>(
        categoriesPath(RestaurantId().value, MenuId().value),
        CreateCategoryRequest(name = "Starters"),
      )
    }

    ex.response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `createCategory should return 422 Unprocessable Entity when the name is blank`() {
    val menu = seedMenu()

    val ex = assertThrows<HttpClientResponseException> {
      httpPost<CreateCategoryResponse>(
        categoriesPath(menu.restaurantId.value, menu.id.value),
        CreateCategoryRequest(name = "   "),
      )
    }

    ex.response.status shouldBe HttpStatus.UNPROCESSABLE_ENTITY
  }
}
