package com.munchies.restaurant.bdd.application.menu

import com.munchies.restaurant.application.MenuService
import com.munchies.restaurant.application.usecase.menu.CreateCategoryCommand
import com.munchies.restaurant.application.usecase.menu.CreateCategoryResult
import com.munchies.restaurant.application.usecase.menu.CreateMenuResult
import com.munchies.restaurant.application.usecase.menu.GetMenuResult
import com.munchies.restaurant.application.usecase.menu.RemoveCategoryCommand
import com.munchies.restaurant.application.usecase.menu.RemoveCategoryResult
import com.munchies.restaurant.application.usecase.menu.UpdateCategoryCommand
import com.munchies.restaurant.application.usecase.menu.UpdateCategoryResult
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
import kotlinx.coroutines.runBlocking

@Singleton
class CategorySteps @Inject constructor(
  private val context: MenuContext,
  private val service: MenuService,
  private val helper: MenuHelper,
) {

  // --- Scenario: Create a new menu category ---

  @Given("the restaurant has a menu")
  fun givenRestaurantHasAMenu() {
    val result = helper.createMenu(
      context.restaurantId,
      "Default",
      "2023-01-01",
      "2023-12-31",
    )
    check(result is CreateMenuResult.Success) { "Menu creation failed" }
    context.menuId = result.menuId
  }

  @When("I create a {string} category in the menu")
  fun addCategory(categoryName: String) {
    val command = CreateCategoryCommand(context.menuId, categoryName)
    context.lastResult = runBlocking { service.createCategory(command) }
  }

  @Then("the category should be created successfully")
  fun categoryCreatedSuccessfully() {
    context.lastResult should beInstanceOf<CreateCategoryResult.Success>()
  }

  @And("the menu should have a {string} category")
  fun menuShouldHaveCategory(categoryName: String) {
    val result = helper.getMenu(context.menuId)
    check(result is GetMenuResult.Success) { "Retrieve menu failed" }
    result.menu.categories shouldExist { it.name.value == categoryName }
  }

  // --- Scenario: Update a menu category ---

  @Given("the menu has a {string} category")
  fun givenMenuHasCategory(categoryName: String) {
    val command = CreateCategoryCommand(context.menuId, categoryName)
    val result = runBlocking { service.createCategory(command) }
    check(result is CreateCategoryResult.Success) { "Category creation failed" }
    context.categoryId = result.categoryId
  }

  @When("I update the {string} category name to {string}")
  fun updateCategoryName(oldName: String, newName: String) {
    check(helper.getCategory(context.menuId, context.categoryId).name.value == oldName) {
      "Category in context doesn't have $oldName as name"
    }
    val command = UpdateCategoryCommand(context.menuId, context.categoryId, newName)
    context.lastResult = runBlocking { service.updateCategory(command) }
  }

  @Then("the category should be updated successfully")
  fun categoryUpdatedSuccessfully() {
    context.lastResult should beInstanceOf<UpdateCategoryResult.Success>()
  }

  // --- Scenario: Remove a menu category ---

  @When("I remove the {string} category from the menu")
  fun removeCategory(categoryName: String) {
    check(helper.getCategory(context.menuId, context.categoryId).name.value == categoryName) {
      "Category in context doesn't have $categoryName as name"
    }
    val command = RemoveCategoryCommand(context.menuId, context.categoryId)
    context.lastResult = runBlocking { service.removeCategory(command) }
  }

  @Then("the category should be removed successfully")
  fun categoryShouldBeRemovedSuccessfully() {
    context.lastResult should beInstanceOf<RemoveCategoryResult.Success>()
  }

  @And("the menu should have no {string} category")
  fun menuShouldHaveNoCategory(categoryName: String) {
    val result = helper.getMenu(context.menuId)
    check(result is GetMenuResult.Success) { "Retrieve menu failed" }
    result.menu.categories.shouldForAll { it.name.value != categoryName }
  }
}
