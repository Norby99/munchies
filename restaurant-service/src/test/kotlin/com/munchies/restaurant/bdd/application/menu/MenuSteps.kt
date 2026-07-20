package com.munchies.restaurant.bdd.application.menu

import com.munchies.restaurant.application.MenuService
import com.munchies.restaurant.application.usecase.menu.*
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.Validity
import com.munchies.restaurant.domain.valueobject.menu.Validity.Period
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.LocalDate.parse
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertThrows

@Singleton
class MenuSteps @Inject constructor(
  private val context: MenuContext,
  private val service: MenuService,
  private val helper: MenuHelper,
) {

  @Given("a restaurant exists")
  fun restaurantExists() {
    context.restaurantId = RestaurantId().value
  }

  // --- Scenario: Create a new menu ---

  @When("I create a {string} menu valid from {word} to {word}")
  fun whenCreateMenu(name: String, start: String, end: String) {
    context.lastResult = helper.createMenu(context.restaurantId, name, start, end)
  }

  @Then("the menu should be created successfully")
  fun menuCreatedSuccessfully() {
    context.lastResult should beInstanceOf<CreateMenuResult.Success>()
  }

  @And("the restaurant should have a {string} menu valid from {word} to {word}")
  fun restaurantShouldHaveAMenuNamed(name: String, start: String, end: String) {
    getRestaurantMenus(context.restaurantId).any {
      it.name.value == name &&
        it.validity == Period(parse(start), parse(end))
    } shouldBe true
  }

  // --- Scenario: Retrieve a menu ---

  @Given("the restaurant has a {string} menu valid from {word} to {word}")
  fun givenRestaurantHasAMenuNamed(name: String, start: String, end: String) {
    val result = helper.createMenu(context.restaurantId, name, start, end)
    check(result is CreateMenuResult.Success) { "Menu creation failed" }
    context.menuId = result.menu.id.value
  }

  @When("I retrieve the menu details for {string}")
  fun whenRetrieveMenuDetails(menuName: String) {
    checkMenuExistence(menuName)
    val command = GetMenuCommand(context.restaurantId, context.menuId)
    context.lastResult = runBlocking { service.getMenu(command) }
  }

  @Then("the menu should be retrieved successfully")
  fun menuRetrievedSuccessfully() {
    context.lastResult should beInstanceOf<GetMenuResult.Success>()
  }

  @And("the menu details should match a {string} menu valid from {word} to {word}")
  fun thenMenuDetailsMatch(name: String, start: String, end: String) {
    val menu = (context.lastResult as GetMenuResult.Success).menu
    menu.name.value shouldBe name
    menu.validity shouldBe Validity.period(start, end)
  }

  // --- Scenario: Update an existing menu ---

  @When("I update the menu {string} to have name {string} and valid from {word} to {word}")
  fun whenUpdateMenuName(name: String, newName: String, start: String, end: String) {
    checkMenuExistence(name)
    val command = UpdateMenuCommand(
      restaurantId = context.restaurantId,
      menuId = context.menuId,
      name = newName,
      validity = ValidityInput.Period(start, end),
    )
    context.lastResult = runBlocking { service.updateMenu(command) }
  }

  @Then("the menu should be updated successfully")
  fun menuShouldBeUpdatedSuccessfully() {
    context.lastResult should beInstanceOf<UpdateMenuResult.Success>()
  }

  // --- Scenario: Remove a menu ---

  @When("I remove the {string} menu")
  fun removeMenu(name: String) {
    checkMenuExistence(name)
    val command = DeleteMenuCommand(context.restaurantId, context.menuId)
    context.lastResult = runBlocking { service.deleteMenu(command) }
  }

  @Then("the menu should be removed successfully")
  fun menuRemovedSuccessfully() {
    context.lastResult should beInstanceOf<DeleteMenuResult.Success>()
  }

  @And("the restaurant should have no {string} menu")
  fun restaurantShouldHaveNoMenu(menuName: String) {
    val result = runBlocking {
      service.getMenu(GetMenuCommand(context.restaurantId, context.menuId))
    }
    check(result is GetMenuResult.MenuNotFound) { "Menu still exists" }
    assertThrows<IllegalStateException> { checkMenuExistence(menuName) }
  }

  private fun checkMenuExistence(menuName: String) {
    val result = helper.getMenu(context.restaurantId, context.menuId)
    check(result is GetMenuResult.Success)
    check(result.menu.name.value == menuName) { "Context doesn't have a $menuName menu" }
  }

  private fun getRestaurantMenus(id: String): List<Menu> {
    val result = runBlocking {
      service
        .getRestaurantMenus(GetRestaurantMenusCommand(id))
    } as GetRestaurantMenusResult.Success
    return result.menus
  }
}
