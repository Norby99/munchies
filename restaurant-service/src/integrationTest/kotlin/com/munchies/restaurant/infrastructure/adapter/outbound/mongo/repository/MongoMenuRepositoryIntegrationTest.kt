package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository

import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.aggregate.MenuItemDetails
import com.munchies.restaurant.domain.valueobject.Money
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.CategoryName
import com.munchies.restaurant.domain.valueobject.menu.MenuItemDescription
import com.munchies.restaurant.domain.valueobject.menu.MenuItemName
import com.munchies.restaurant.domain.valueobject.menu.MenuName
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.context.ApplicationContext
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.mongodb.MongoDBContainer

/**
 * Integration test: exercises [MongoMenuRepository] against a real MongoDB instance
 * (via Testcontainers) to verify the persistence adapter maps the [Menu] aggregate,
 * including its nested categories and items, correctly to and from the document model.
 * No HTTP layer or use cases are involved.
 */
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MongoMenuRepositoryIntegrationTest {

  companion object {
    @Container
    @JvmStatic
    private val mongo = MongoDBContainer("mongo:7.0")
  }

  private lateinit var context: ApplicationContext
  private lateinit var repository: MongoMenuRepository

  @BeforeAll
  fun setup() {
    context = ApplicationContext.run(
      mapOf(
        "mongodb.uri" to "${mongo.connectionString}/restaurant-service",
        "mongodb.package-names[0]" to
          "com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document",
      ),
      "prod",
    )
    repository = context.getBean(MongoMenuRepository::class.java)
  }

  /**
   * Clean up the database after each test to ensure isolation between tests.
   */
  @AfterEach
  fun cleanup() {
    context.getBean(MongoCrudMenuRepository::class.java).deleteAll()
  }

  @AfterAll
  fun tearDown() {
    context.close()
  }

  private fun menuWithOneItem(): Menu {
    val menu = Menu.create(RestaurantId(), MenuName.of("Lunch"))
    val category = menu.createCategory(CategoryName.of("Starters"))
    category.createItem(
      MenuItemDetails(
        MenuItemName.of("Salad"),
        MenuItemDescription.of("Fresh greens"),
      ),
      Money("5.50"),
    )
    return menu
  }

  @Test
  fun `saves and retrieves a menu with its categories and items`() {
    val menu = menuWithOneItem()

    repository.save(menu)
    val found = repository.findById(menu.id)

    found shouldBe menu
    found!!.name.value shouldBe "Lunch"
    found.categories shouldHaveSize 1
    found.categories.first().items shouldHaveSize 1
    found.categories.first().items.first().name.value shouldBe "Salad"
  }

  @Test
  fun `findById returns null when the menu does not exist`() {
    repository.findById(MenuId()).shouldBeNull()
  }

  @Test
  fun `findByIdAndRestaurantId returns null when the menu belongs to another restaurant`() {
    val menu = menuWithOneItem()
    repository.save(menu)

    repository.findByIdAndRestaurantId(menu.id, RestaurantId()).shouldBeNull()
  }

  @Test
  fun `update persists changes made to a category`() {
    val menu = menuWithOneItem()
    repository.save(menu)

    menu.updateCategory(menu.categories.first().id, CategoryName.of("Mains"))
    repository.update(menu)

    repository.findById(menu.id)!!.categories.first().name.value shouldBe "Mains"
  }

  @Test
  fun `delete removes the menu`() {
    val menu = menuWithOneItem()
    repository.save(menu)

    repository.delete(menu)

    repository.findById(menu.id).shouldBeNull()
  }
}
