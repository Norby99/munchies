package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.domain.aggregate.Category
import com.munchies.restaurant.domain.aggregate.CategoryId
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.aggregate.MenuItem
import com.munchies.restaurant.domain.aggregate.MenuItemDetails
import com.munchies.restaurant.domain.aggregate.MenuItemId
import com.munchies.restaurant.domain.valueobject.Money
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.CategoryName
import com.munchies.restaurant.domain.valueobject.menu.MenuItemDescription
import com.munchies.restaurant.domain.valueobject.menu.MenuItemName
import com.munchies.restaurant.domain.valueobject.menu.MenuName
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.DeleteMenuItemResponse
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
class DeleteMenuItemControllerComponentTest : BaseMenuController() {

  @Inject
  lateinit var menuRepository: MongoMenuRepository

  @Inject
  lateinit var mongoCrudMenuRepository: MongoCrudMenuRepository

  @AfterEach
  fun cleanupMongo() {
    mongoCrudMenuRepository.deleteAll()
  }

  private fun seedMenuWithItem(): Triple<Menu, Category, MenuItem> {
    val menu = Menu.create(RestaurantId(), MenuName.of("Lunch"))
    val category = menu.createCategory(CategoryName.of("Starters"))
    val item = category.createItem(
      MenuItemDetails(MenuItemName.of("Salad"), MenuItemDescription.of("Fresh greens")),
      Money("5.50"),
    )
    menuRepository.save(menu)
    return Triple(menu, category, item)
  }

  private fun itemPath(restaurantId: String, menuId: String, categoryId: String, itemId: String) =
    "/restaurant/$restaurantId/menus/$menuId/categories/$categoryId/items/$itemId"

  @Test
  fun `removeMenuItem should return 200 OK and remove the item from the category`() {
    val (menu, category, item) = seedMenuWithItem()

    val response = httpDelete<DeleteMenuItemResponse>(
      itemPath(menu.restaurantId.value, menu.id.value, category.id.value, item.id.value),
    )

    response.status shouldBe HttpStatus.OK
    menuRepository.findById(menu.id)!!.categories.first().items shouldHaveSize 0
  }

  @Test
  fun `removeMenuItem should return 404 Not Found when the menu does not exist`() {
    val ex = assertThrows<HttpClientResponseException> {
      httpDelete<DeleteMenuItemResponse>(
        itemPath(
          RestaurantId().value,
          MenuId().value,
          CategoryId().value,
          MenuItemId().value,
        ),
      )
    }

    ex.response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `removeMenuItem should return 404 Not Found when the category does not exist`() {
    val menu = Menu.create(RestaurantId(), MenuName.of("Lunch"))
    menuRepository.save(menu)

    val ex = assertThrows<HttpClientResponseException> {
      httpDelete<DeleteMenuItemResponse>(
        itemPath(menu.restaurantId.value, menu.id.value, CategoryId().value, MenuItemId().value),
      )
    }

    ex.response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `removeMenuItem is idempotent - deleting an already-absent item still returns 200 OK`() {
    val menu = Menu.create(RestaurantId(), MenuName.of("Lunch"))
    val category = menu.createCategory(CategoryName.of("Starters"))
    menuRepository.save(menu)

    val response = httpDelete<DeleteMenuItemResponse>(
      itemPath(menu.restaurantId.value, menu.id.value, category.id.value, MenuItemId().value),
    )

    response.status shouldBe HttpStatus.OK
  }
}
