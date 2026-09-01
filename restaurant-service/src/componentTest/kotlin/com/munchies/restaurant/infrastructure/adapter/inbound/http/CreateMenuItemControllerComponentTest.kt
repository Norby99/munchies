package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.restaurant.domain.aggregate.CategoryId
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.CategoryName
import com.munchies.restaurant.domain.valueobject.menu.MenuName
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.CreateMenuItemRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.CreateMenuItemResponse
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository.MongoCrudMenuRepository
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository.MongoMenuRepository
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Component test: boots the full restaurant-service Micronaut application (real HTTP
 * server, real Mongo via Testcontainers, real controller/mapper/use-case/repository
 * wiring) and drives it purely through its public HTTP contract. Unlike the unit test
 * for [com.munchies.restaurant.application.usecase.menu.CreateMenuItemUseCase] (which
 * mocks the repository) or the integration test for [MongoMenuRepository] (which
 * bypasses HTTP entirely), this verifies the whole vertical slice - request parsing,
 * mapping, domain logic, persistence and error responses - behaves correctly together.
 */
@MicronautTest(environments = ["prod"], transactional = false)
class CreateMenuItemControllerComponentTest : BaseMenuController() {

  @Inject
  lateinit var menuRepository: MongoMenuRepository

  @Inject
  lateinit var mongoCrudMenuRepository: MongoCrudMenuRepository

  @AfterEach
  fun cleanupMongo() {
    mongoCrudMenuRepository.deleteAll()
  }

  private fun seedMenuWithCategory(): Menu {
    val menu = Menu.create(RestaurantId(), MenuName.of("Lunch"))
    menu.createCategory(CategoryName.of("Starters"))
    menuRepository.save(menu)
    return menu
  }

  private fun itemsPath(restaurantId: String, menuId: String, categoryId: String) =
    "/restaurant/$restaurantId/menus/$menuId/categories/$categoryId/items"

  // ==========================================
  // TEST: POST /restaurant/{restaurantId}/menus/{menuId}/categories/{categoryId}/items
  // ==========================================

  @Test
  fun `createMenuItem should return 201 Created and persist the item under its category`() {
    val menu = seedMenuWithCategory()
    val categoryId = menu.categories.first().id.value
    val request = CreateMenuItemRequest(
      name = "Salad",
      description = "Fresh green salad",
      price = "5.50",
    )

    val response = httpPost<CreateMenuItemResponse>(
      itemsPath(menu.restaurantId.value, menu.id.value, categoryId),
      request,
    )

    response.status shouldBe HttpStatus.CREATED
    response.body()!!.code shouldBe HttpStatus.CREATED.code

    val persisted = menuRepository.findById(menu.id)!!
    persisted.categories.first().items shouldHaveSize 1
    persisted.categories.first().items.first().name.value shouldBe "Salad"
  }

  @Test
  fun `createMenuItem should return 404 Not Found when the menu does not exist`() {
    val request = CreateMenuItemRequest(name = "Salad", description = "Fresh", price = "5.50")

    val ex = assertThrows<HttpClientResponseException> {
      httpPost<CreateMenuItemResponse>(
        itemsPath(RestaurantId().value, MenuId().value, CategoryId().value),
        request,
      )
    }

    ex.response.status shouldBe HttpStatus.NOT_FOUND
    ex.response.bd<ErrorResponse>().result shouldBe "Menu not found"
  }

  @Test
  fun `createMenuItem should return 404 Not Found when the category does not exist`() {
    val menu = Menu.create(RestaurantId(), MenuName.of("Lunch"))
    menuRepository.save(menu)
    val request = CreateMenuItemRequest(name = "Salad", description = "Fresh", price = "5.50")

    val ex = assertThrows<HttpClientResponseException> {
      httpPost<CreateMenuItemResponse>(
        itemsPath(menu.restaurantId.value, menu.id.value, CategoryId().value),
        request,
      )
    }

    ex.response.status shouldBe HttpStatus.NOT_FOUND
    ex.response.bd<ErrorResponse>().result shouldBe "Category not found"
  }

  @Test
  fun `createMenuItem should return 422 Unprocessable Entity on a negative price`() {
    val menu = seedMenuWithCategory()
    val categoryId = menu.categories.first().id.value
    val request = CreateMenuItemRequest(name = "Salad", description = "Fresh", price = "-1")

    val ex = assertThrows<HttpClientResponseException> {
      httpPost<CreateMenuItemResponse>(
        itemsPath(menu.restaurantId.value, menu.id.value, categoryId),
        request,
      )
    }

    ex.response.status shouldBe HttpStatus.UNPROCESSABLE_ENTITY
    menuRepository.findById(menu.id)!!.categories.first().items.shouldHaveSize(0)
  }
}
