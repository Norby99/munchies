package com.munchies.restaurant.bdd.application

import com.munchies.restaurant.application.usecase.restaurant.CreateRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.DeleteRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.GetRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.UpdateRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.UpdateRestaurantResult
import com.munchies.restaurant.domain.valueobject.UserId
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class RestaurantSteps @Inject constructor(
  private val context: RestaurantContext,
  private val helper: RestaurantHelper,
) {

  @Given("I am a manager user")
  fun iAmAManagerUser() {
    context.managerId = UserId().value
  }

  // --- Scenario: Create a new restaurant ---
  @When("I create a restaurant with:")
  fun createRestaurant(details: DataTable) {
    val detailsRow = details.asMaps().first()
    val result = helper.createRestaurant(
      context.managerId!!,
      detailsRow["name"]!!,
      detailsRow["address"]!!,
      detailsRow["phone"]!!,
      detailsRow["email"]!!,
    )
    context.lastResult = result
  }

  @Then("the restaurant should be created successfully")
  fun restaurantCreatedSuccessfully() {
    context.lastResult should beInstanceOf<CreateRestaurantResult.Success>()
  }

  @And("I should have a restaurant with:")
  fun iShouldHaveARestaurantWith(details: DataTable) {
    val detailsRow = details.asMaps().first()
    val restaurants = helper.getManagerRestaurants(context.managerId!!)

    restaurants shouldExist { restaurant ->
      restaurant.name.value == detailsRow["name"]!! &&
        restaurant.address.value == detailsRow["address"]!! &&
        restaurant.phone.value == detailsRow["phone"]!! &&
        restaurant.email.value == detailsRow["email"]!!
    }
  }

  // --- Scenario: Retrieve created restaurant details ---
  @Given("I created a restaurant with:")
  fun createdRestaurant(details: DataTable) {
    val detailsRow = details.asMaps().first()
    val result = helper.createRestaurant(
      context.managerId!!,
      detailsRow["name"]!!,
      detailsRow["address"]!!,
      detailsRow["phone"]!!,
      detailsRow["email"]!!,
    )
    require(result is CreateRestaurantResult.Success) { "Restaurant creation failed" }
    context.restaurantId = result.restaurantId
  }

  @When("I retrieve the restaurant details")
  fun retrieveRestaurantDetails() {
    context.lastResult = helper.getRestaurantDetails(context.restaurantId!!)
  }

  @Then("the restaurant should be retrieved successfully")
  fun restaurantDetailsRetrievedSuccessfully() {
    context.lastResult should beInstanceOf<GetRestaurantResult.Success>()
  }

  @And("the restaurant details should match:")
  fun restaurantDetailsMatch(details: DataTable) {
    val detailsRow = details.asMaps().first()
    val restaurant = (context.lastResult as GetRestaurantResult.Success).restaurant

    restaurant.name.value shouldBe detailsRow["name"]!!
    restaurant.address.value shouldBe detailsRow["address"]!!
    restaurant.phone.value shouldBe detailsRow["phone"]!!
    restaurant.email.value shouldBe detailsRow["email"]!!
  }

  // --- Scenario: Update restaurant information ---
  @When("I update the restaurant details to:")
  fun updateRestaurantTo(dataTable: DataTable) {
    val detailsRow = dataTable.asMaps().first()

    context.lastResult = helper.updateRestaurant(
      UpdateRestaurantCommand(
        context.restaurantId!!,
        context.managerId!!,
        detailsRow["name"]!!,
        detailsRow["address"]!!,
        detailsRow["phone"]!!,
        detailsRow["email"]!!,
      ),
    )
  }

  @Then("the restaurant should be updated successfully")
  fun restaurantUpdateSuccessful() {
    context.lastResult should beInstanceOf<UpdateRestaurantResult.Success>()
  }

  // Scenario: Delete restaurant
  @When("I delete the restaurant")
  fun deleteRestaurant() {
    context.lastResult = helper.deleteRestaurant(
      context.restaurantId!!,
      context.managerId!!,
    )
  }

  @Then("the restaurant should be deleted successfully")
  fun restaurantDeletedSuccessfully() {
    context.lastResult should beInstanceOf<DeleteRestaurantResult.Success>()
  }

  @And("I should have no restaurant with:")
  fun iShouldHaveNoRestaurantWith(details: DataTable) {
    val detailsRow = details.asMaps().first()
    val restaurants = helper.getManagerRestaurants(context.managerId!!)

    restaurants.none { restaurant ->
      restaurant.name.value == detailsRow["name"]!! &&
        restaurant.address.value == detailsRow["address"]!! &&
        restaurant.phone.value == detailsRow["phone"]!! &&
        restaurant.email.value == detailsRow["email"]!!
    } shouldBe true
  }
}
