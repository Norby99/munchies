package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.domain.aggregate.Category
import com.munchies.restaurant.domain.aggregate.CategoryId
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.CategoryName
import com.munchies.restaurant.domain.valueobject.menu.MenuName
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.UpdateCategoryRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.UpdateCategoryResponse
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
class UpdateCategoryControllerComponentTest : BaseMenuController() {

  @Inject
  lateinit var menuRepository: MongoMenuRepository

  @Inject
  lateinit var mongoCrudMenuRepository: MongoCrudMenuRepository

  @AfterEach
  fun cleanupMongo() {
    mongoCrudMenuRepository.deleteAll()
  }

  private fun seedMenuWithCategory(): Pair<Menu, Category> {
    val menu = Menu.create(RestaurantId(), MenuName.of("Lunch"))
    val category = menu.createCategory(CategoryName.of("Starters"))
    menuRepository.save(menu)
    return menu to category
  }

  private fun categoryPath(restaurantId: String, menuId: String, categoryId: String) =
    "/restaurant/$restaurantId/menus/$menuId/categories/$categoryId"

  @Test
  fun `updateCategory should return 200 OK and persist the new name`() {
    val (menu, category) = seedMenuWithCategory()

    val response = httpPut<UpdateCategoryResponse>(
      categoryPath(menu.restaurantId.value, menu.id.value, category.id.value),
      UpdateCategoryRequest(name = "Antipasti"),
    )

    response.status shouldBe HttpStatus.OK
    response.body()!!.result.name shouldBe "Antipasti"
    menuRepository.findById(menu.id)!!.categories.first().name.value shouldBe "Antipasti"
  }

  @Test
  fun `updateCategory should return 404 Not Found when the menu does not exist`() {
    val ex = assertThrows<HttpClientResponseException> {
      httpPut<UpdateCategoryResponse>(
        categoryPath(RestaurantId().value, MenuId().value, CategoryId().value),
        UpdateCategoryRequest(name = "Antipasti"),
      )
    }

    ex.response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `updateCategory should return 422 Unprocessable Entity when category does not exist`() {
    val menu = Menu.create(RestaurantId(), MenuName.of("Lunch"))
    menuRepository.save(menu)

    val ex = assertThrows<HttpClientResponseException> {
      httpPut<UpdateCategoryResponse>(
        categoryPath(menu.restaurantId.value, menu.id.value, CategoryId().value),
        UpdateCategoryRequest(name = "Antipasti"),
      )
    }

    // The use case only validates existence inside the aggregate, so a missing
    // category surfaces as a domain validation failure (422), not a 404 - unlike
    // a missing menu, which is checked up front by the repository lookup.
    ex.response.status shouldBe HttpStatus.UNPROCESSABLE_ENTITY
  }
}
