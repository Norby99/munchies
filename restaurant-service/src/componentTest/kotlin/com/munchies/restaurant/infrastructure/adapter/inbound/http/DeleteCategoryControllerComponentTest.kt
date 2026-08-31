package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.domain.aggregate.Category
import com.munchies.restaurant.domain.aggregate.CategoryId
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.CategoryName
import com.munchies.restaurant.domain.valueobject.menu.MenuName
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.DeleteCategoryResponse
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

@MicronautTest(environments = ["prod"], transactional = false)
class DeleteCategoryControllerComponentTest : BaseMenuController() {

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
  fun `deleteCategory should return 200 OK and remove the category from the menu`() {
    val (menu, category) = seedMenuWithCategory()

    val response = httpDelete<DeleteCategoryResponse>(
      categoryPath(menu.restaurantId.value, menu.id.value, category.id.value),
    )

    response.status shouldBe HttpStatus.OK
    menuRepository.findById(menu.id)!!.categories shouldHaveSize 0
  }

  @Test
  fun `deleteCategory should return 404 Not Found when the menu does not exist`() {
    val ex = assertThrows<HttpClientResponseException> {
      httpDelete<DeleteCategoryResponse>(
        categoryPath(RestaurantId().value, MenuId().value, CategoryId().value),
      )
    }

    ex.response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `deleteCategory is idempotent - deleting an already-absent category still returns 200 OK`() {
    val menu = Menu.create(RestaurantId(), MenuName.of("Lunch"))
    menuRepository.save(menu)

    val response = httpDelete<DeleteCategoryResponse>(
      categoryPath(menu.restaurantId.value, menu.id.value, CategoryId().value),
    )

    response.status shouldBe HttpStatus.OK
  }
}
