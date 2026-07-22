package com.munchies.restaurant.bdd.application.menu

import com.munchies.restaurant.application.MenuService
import com.munchies.restaurant.application.usecase.menu.UpdateCategoryCommand
import com.munchies.restaurant.application.usecase.menu.UpdateCategoryResult
import com.munchies.restaurant.application.usecase.menu.VariationInput
import com.munchies.restaurant.application.usecase.menu.VariationOptionInput
import com.munchies.restaurant.application.usecase.menu.toInput
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
class CategoryVariationSteps @Inject constructor(
  private val context: MenuContext,
  private val service: MenuService,
  private val helper: MenuHelper,
) {

  // --- Scenario: Create a variation to a menu category ---

  @When("I create in the {string} category a {string} variation with options:")
  fun createVariation(categoryName: String, variationName: String, optionsTable: DataTable) {
    val category = helper.getCategory(context)
    check(category.name.value == categoryName) { "Category name mismatch" }

    val options = optionsTable.asMaps()
      .map { VariationOptionInput(it["name"]!!, BigDecimal(it["price"]!!)) }

    val oldVariations = category.variations.map { it.toInput() }
    val command = UpdateCategoryCommand(
      restaurantId = context.restaurantId,
      menuId = context.menuId,
      categoryId = category.id.value,
      name = category.name.value,
      variations = oldVariations + VariationInput(variationName, options),
    )
    context.lastResult = runBlocking { service.updateCategory(command) }
  }

  @Then("the variation should be created successfully for the category")
  fun variationCreated() {
    context.lastResult should beInstanceOf<UpdateCategoryResult.Success>()
  }

  @And("the {string} category should have a {string} variation with options:")
  fun categoryHasVariation(categoryName: String, variationName: String, optionsTable: DataTable) {
    val category = helper.getCategory(context)
    check(category.name.value == categoryName) { "Category name mismatch" }

    val expected = optionsTable.asMaps()
      .map { VariationOptionInput(it["name"]!!, BigDecimal(it["price"]!!)) }

    category.variations shouldExist { variation ->
      variation.name.value == variationName &&
        variation.options.map { it.toInput() } == expected
    }
  }

  // --- Scenario: Update an existing variation of a menu category ---

  @Given("the {string} category has a {string} variation with options:")
  fun hasVariation(categoryName: String, variationName: String, optionsTable: DataTable) {
    val category = helper.getCategory(context)
    check(category.name.value == categoryName) { "Category name mismatch" }

    val options = optionsTable.asMaps()
      .map { VariationOptionInput(it["name"]!!, BigDecimal(it["price"]!!)) }

    val oldVariations = category.variations.map { it.toInput() }
    val command = UpdateCategoryCommand(
      restaurantId = context.restaurantId,
      menuId = context.menuId,
      categoryId = category.id.value,
      name = category.name.value,
      variations = oldVariations + VariationInput(variationName, options),
    )
    val result = runBlocking { service.updateCategory(command) }
    check(result is UpdateCategoryResult.Success) { "Variation creation failed" }
  }

  @When("I update the {string} variation for the {string} category to have options:")
  fun updateVariation(variationName: String, categoryName: String, optionsTable: DataTable) {
    val category = helper.getCategory(context)
    check(category.name.value == categoryName) { "Category name mismatch" }

    val newOptions = optionsTable.asMaps()
      .map { VariationOptionInput(it["name"]!!, BigDecimal(it["price"]!!)) }

    val command = UpdateCategoryCommand(
      restaurantId = context.restaurantId,
      menuId = context.menuId,
      categoryId = category.id.value,
      name = category.name.value,
      variations = category.variations.map {
        if (it.name.value == variationName) {
          VariationInput(variationName, newOptions)
        } else {
          it.toInput()
        }
      },
    )
    context.lastResult = runBlocking { service.updateCategory(command) }
  }

  @Then("the variation should be updated successfully for the category")
  fun variationUpdated() {
    context.lastResult should beInstanceOf<UpdateCategoryResult.Success>()
  }

  // --- Scenario: Remove a variation from a menu category ---

  @When("I remove the {string} variation from the {string} category")
  fun removeVariation(variationName: String, categoryName: String) {
    val category = helper.getCategory(context)
    check(category.name.value == categoryName) { "Category name mismatch" }

    val command = UpdateCategoryCommand(
      restaurantId = context.restaurantId,
      menuId = context.menuId,
      categoryId = category.id.value,
      name = category.name.value,
      variations = category.variations
        .filter { it.name.value != variationName }
        .map { it.toInput() },
    )
    context.lastResult = runBlocking { service.updateCategory(command) }
  }

  @Then("the variation should be removed successfully from the category")
  fun variationRemoved() {
    context.lastResult should beInstanceOf<UpdateCategoryResult.Success>()
  }

  @And("the {string} category should have no {string} variation")
  fun categoryHasNoVariation(categoryName: String, variationName: String) {
    val category = helper.getCategory(context)
    check(category.name.value == categoryName) { "Category name mismatch" }

    category.variations.shouldForAll { it.name.value != variationName }
  }
}
