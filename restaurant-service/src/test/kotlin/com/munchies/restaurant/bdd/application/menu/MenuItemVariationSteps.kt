package com.munchies.restaurant.bdd.application.menu

import com.munchies.restaurant.application.MenuService
import com.munchies.restaurant.application.usecase.menu.CreateMenuItemCommand
import com.munchies.restaurant.application.usecase.menu.CreateMenuItemResult
import com.munchies.restaurant.application.usecase.menu.UpdateMenuItemCommand
import com.munchies.restaurant.application.usecase.menu.UpdateMenuItemResult
import com.munchies.restaurant.application.usecase.menu.VariationDto
import com.munchies.restaurant.application.usecase.menu.VariationOptionDto
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
class MenuItemVariationSteps @Inject constructor(
  private val context: MenuContext,
  private val service: MenuService,
  private val helper: MenuHelper,
) {

  // --- Background: Setup Item ---

  @Given("the {string} category has an {string} item with description {string} and price {double}")
  fun givenCategoryHasItem(
    categoryName: String,
    itemName: String,
    description: String,
    price: Double,
  ) {
    val category = helper.getCategory(context.menuId, context.categoryId)
    check(category.name.value == categoryName) { "Category name mismatch" }

    val command = CreateMenuItemCommand(
      menuId = context.menuId,
      categoryId = context.categoryId,
      name = itemName,
      description = description,
      price = BigDecimal.valueOf(price),
    )
    val result = runBlocking { service.createMenuItem(command) }
    check(result is CreateMenuItemResult.Success) { "Item creation failed" }
    context.itemId = result.itemId
  }

  // --- Scenario: Create a variation to a specific menu item ---

  @When("I create in the {string} item a {string} variation with options:")
  fun createVariation(itemName: String, variationName: String, optionsTable: DataTable) {
    val category = helper.getCategory(context.menuId, context.categoryId)
    val item = category.items.first { it.id.value == context.itemId }
    check(item.name.value == itemName) { "Item name mismatch" }

    val options = optionsTable.asMaps()
      .map { VariationOptionDto(it["name"]!!, BigDecimal(it["price"]!!)) }

    val oldVariations = item.variations.map(::VariationDto)
    val command = UpdateMenuItemCommand(
      menuId = context.menuId,
      categoryId = context.categoryId,
      itemId = item.id.value,
      name = item.name.value,
      description = item.description.value,
      price = item.price.amount,
      variations = oldVariations + VariationDto(variationName, options),
    )
    context.lastResult = runBlocking { service.updateMenuItem(command) }
  }

  @Then("the variation should be created successfully to the item")
  fun variationCreated() {
    context.lastResult should beInstanceOf<UpdateMenuItemResult.Success>()
  }

  @And("the {string} item should have a {string} variation with options:")
  fun itemHasVariation(itemName: String, variationName: String, optionsTable: DataTable) {
    val category = helper.getCategory(context.menuId, context.categoryId)
    val item = category.items.first { it.id.value == context.itemId }
    check(item.name.value == itemName) { "Item name mismatch" }

    val expected = optionsTable.asMaps()
      .map { VariationOptionDto(it["name"]!!, BigDecimal(it["price"]!!)) }

    item.variations shouldExist { variation ->
      variation.name.value == variationName &&
        variation.options.map(::VariationOptionDto) == expected
    }
  }

  // --- Scenario: Update an existing variation of a menu item ---

  @Given("the {string} item has a {string} variation with options:")
  fun hasVariation(itemName: String, variationName: String, optionsTable: DataTable) {
    val category = helper.getCategory(context.menuId, context.categoryId)
    val item = category.items.first { it.id.value == context.itemId }
    check(item.name.value == itemName) { "Item name mismatch" }

    val options = optionsTable.asMaps()
      .map { VariationOptionDto(it["name"]!!, BigDecimal(it["price"]!!)) }

    val oldVariations = item.variations.map(::VariationDto)
    val command = UpdateMenuItemCommand(
      menuId = context.menuId,
      categoryId = context.categoryId,
      itemId = item.id.value,
      name = item.name.value,
      description = item.description.value,
      price = item.price.amount,
      variations = oldVariations + VariationDto(variationName, options),
    )
    val result = runBlocking { service.updateMenuItem(command) }
    check(result is UpdateMenuItemResult.Success) { "Item variation creation failed" }
  }

  @When("I update the {string} variation for the {string} item to have options:")
  fun updateVariation(variationName: String, itemName: String, optionsTable: DataTable) {
    val category = helper.getCategory(context.menuId, context.categoryId)
    val item = category.items.first { it.id.value == context.itemId }
    check(item.name.value == itemName) { "Item name mismatch" }

    val newOptions = optionsTable.asMaps()
      .map { VariationOptionDto(it["name"]!!, BigDecimal(it["price"]!!)) }

    val command = UpdateMenuItemCommand(
      menuId = context.menuId,
      categoryId = context.categoryId,
      itemId = item.id.value,
      name = item.name.value,
      description = item.description.value,
      price = item.price.amount,
      variations = item.variations.map {
        if (it.name.value == variationName) {
          VariationDto(
            variationName,
            newOptions,
          )
        } else {
          VariationDto(it)
        }
      },
    )
    context.lastResult = runBlocking { service.updateMenuItem(command) }
  }

  @Then("the variation should be updated successfully for the item")
  fun variationUpdated() {
    context.lastResult should beInstanceOf<UpdateMenuItemResult.Success>()
  }

  // --- Scenario: Remove a variation from a menu item ---

  @When("I remove the {string} variation from the {string} item")
  fun removeVariation(variationName: String, itemName: String) {
    val category = helper.getCategory(context.menuId, context.categoryId)
    val item = category.items.first { it.id.value == context.itemId }
    check(item.name.value == itemName) { "Item name mismatch" }

    val command = UpdateMenuItemCommand(
      menuId = context.menuId,
      categoryId = context.categoryId,
      itemId = item.id.value,
      name = item.name.value,
      description = item.description.value,
      price = item.price.amount,
      variations = item.variations.filter { it.name.value != variationName }.map(::VariationDto),
    )
    context.lastResult = runBlocking { service.updateMenuItem(command) }
  }

  @Then("the variation should be removed successfully")
  fun variationRemoved() {
    context.lastResult should beInstanceOf<UpdateMenuItemResult.Success>()
  }

  @And("the {string} item should have no {string} variation")
  fun itemHasNoVariation(itemName: String, variationName: String) {
    val category = helper.getCategory(context.menuId, context.categoryId)
    val item = category.items.first { it.id.value == context.itemId }
    check(item.name.value == itemName) { "Item name mismatch" }

    item.variations.shouldForAll { it.name.value != variationName }
  }
}
