package com.munchies.restaurant.bdd.application.menu

import com.munchies.restaurant.application.MenuService
import com.munchies.restaurant.application.usecase.menu.CreateMenuItemCommand
import com.munchies.restaurant.application.usecase.menu.CreateMenuItemResult
import com.munchies.restaurant.application.usecase.menu.RemoveMenuItemCommand
import com.munchies.restaurant.application.usecase.menu.RemoveMenuItemResult
import com.munchies.restaurant.application.usecase.menu.UpdateMenuItemCommand
import com.munchies.restaurant.application.usecase.menu.UpdateMenuItemResult
import com.munchies.restaurant.domain.valueobject.Money
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.should
import io.kotest.matchers.types.beInstanceOf
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.math.BigDecimal
import kotlinx.coroutines.runBlocking

@Singleton
class MenuItemSteps @Inject constructor(
  private val context: MenuContext,
  private val service: MenuService,
  private val helper: MenuHelper,
) {

  // --- Scenario: Add an item to a category ---

  @When("I create in the {string} category a {string} item with:")
  fun createMenuItem(categoryName: String, itemName: String, details: DataTable) {
    check(helper.getCategory(context.menuId, context.categoryId).name.value == categoryName) {
      "Category in context doesn't have $categoryName as name"
    }
    context.lastResult = createMenuItem(
      context.menuId,
      context.categoryId,
      itemName,
      details.asMaps().first(),
    )
  }

  @Then("the item should be created successfully")
  fun itemCreatedSuccessfully() {
    context.lastResult should beInstanceOf<CreateMenuItemResult.Success>()
  }

  @And("the {string} category should have a {string} item with:")
  fun categoryShouldHaveItem(categoryName: String, itemName: String, details: DataTable) {
    val category = helper.getCategory(context.menuId, context.categoryId)
    check(category.name.value == categoryName) { "Category name mismatch" }
    val detailsMap = details.asMaps().first()
    category.items shouldExist {
      it.name.value == itemName &&
        it.description.value == detailsMap["description"]!! &&
        it.price == Money(BigDecimal(detailsMap["price"]!!))
    }
  }

  // --- Scenario: Update an existing menu item ---

  @Given("the {string} category has a {string} item with:")
  fun givenCategoryHasItem(categoryName: String, itemName: String, details: DataTable) {
    check(helper.getCategory(context.menuId, context.categoryId).name.value == categoryName) {
      "Category in context doesn't have $categoryName as name"
    }
    val result = createMenuItem(
      context.menuId,
      context.categoryId,
      itemName,
      details.asMaps().first(),
    )
    check(result is CreateMenuItemResult.Success) { "Item creation failed" }
    context.itemId = result.itemId
  }

  @When("I update in the {string} category the {string} item to have:")
  fun whenUpdateItemPrice(categoryName: String, itemName: String, details: DataTable) {
    check(helper.getCategory(context.menuId, context.categoryId).name.value == categoryName) {
      "Category in context doesn't have $categoryName as name"
    }
    context.lastResult = updateMenuItem(
      context.menuId,
      context.categoryId,
      context.itemId,
      itemName,
      details.asMaps().first(),
    )
  }

  @Then("the item should be updated successfully")
  fun itemShouldBeUpdatedSuccessfully() {
    context.lastResult should beInstanceOf<UpdateMenuItemResult.Success>()
  }

  // --- Scenario: Remove a menu item ---

  @When("I remove from the {string} category the {string} item")
  fun removeMenuItem(categoryName: String, itemName: String) {
    val category = helper.getCategory(context.menuId, context.categoryId)
    check(category.name.value == categoryName) {
      "Category in context doesn't have $categoryName as name"
    }
    check(category.items.any { it.name.value == itemName }) { "No $itemName item found" }
    val command = RemoveMenuItemCommand(context.menuId, context.categoryId, context.itemId)
    context.lastResult = runBlocking { service.removeMenuItem(command) }
  }

  @Then("the item should be removed successfully")
  fun thenItemRemovedSuccessfully() {
    context.lastResult should beInstanceOf<RemoveMenuItemResult.Success>()
  }

  @And("the {string} category should have no {string} item")
  fun andCategoryHasNoMenuItem(categoryName: String, itemName: String) {
    val category = helper.getCategory(context.menuId, context.categoryId)
    check(category.name.value == categoryName) { "Category name mismatch" }
    category.items.shouldForAll { it.name.value != itemName }
  }

  // --- Helpers ---

  private fun createMenuItem(
    menuId: String,
    categoryId: String,
    name: String,
    details: Map<String, String>,
  ): CreateMenuItemResult {
    val command = CreateMenuItemCommand(
      menuId,
      categoryId,
      name,
      details["description"]!!,
      BigDecimal(details["price"]!!),
    )
    return runBlocking { service.createMenuItem(command) }
  }

  private fun updateMenuItem(
    menuId: String,
    categoryId: String,
    itemId: String,
    name: String,
    details: Map<String, String>,
  ): UpdateMenuItemResult {
    val command = UpdateMenuItemCommand(
      menuId,
      categoryId,
      itemId,
      name,
      details["description"]!!,
      BigDecimal(details["price"]!!),
    )
    return runBlocking { service.updateMenuItem(command) }
  }
}
