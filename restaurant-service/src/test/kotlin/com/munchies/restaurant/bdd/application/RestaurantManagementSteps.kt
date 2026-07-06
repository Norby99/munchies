package com.munchies.restaurant.bdd.application

import com.munchies.restaurant.application.usecase.restaurant.CreateRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.CreateRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.DeleteRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.GetRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.UpdateRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.UpdateRestaurantResult
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.kotest.matchers.types.beInstanceOf
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class RestaurantManagementSteps @Inject constructor(
  private val context: RestaurantContext,
  private val helper: RestaurantHelper,
) {

  // Scenario: Create a new restaurant
  @When(
    "I create a restaurant with name {string}, address {string}, phone {string}," +
      " and email {string}",
  )
  fun createRestaurant(name: String, address: String, phone: String, email: String) {
    context.restaurantName = name
    context.restaurantAddress = address
    context.restaurantPhone = phone
    context.restaurantEmail = email
    val command = CreateRestaurantCommand(
      managerId = context.currentUserId,
      name = name,
      address = address,
      phone = phone,
      email = email,
    )
    val result = helper.createRestaurant(command)
    context.lastResult = result
    context.createdRestaurantId = (result as? CreateRestaurantResult.Success)?.restaurantId
      ?: throw AssertionError("Restaurant creation should be successful")
    context.managerId = context.currentUserId
  }

  @Then("the restaurant should be created successfully")
  fun restaurantCreatedSuccessfully() {
    context.lastResult should beInstanceOf<CreateRestaurantResult.Success>()
    val result = context.lastResult as CreateRestaurantResult.Success
    result.restaurantId.shouldNotBeBlank()
  }

  // Scenario: Retrieve created restaurant details
  @Given(
    "I created a restaurant with name {string}, address {string}, phone {string}," +
      " and email {string}",
  )
  fun createdRestaurant(name: String, address: String, phone: String, email: String) {
    createRestaurant(name, address, phone, email)
  }

  @When("I retrieve the restaurant details")
  fun retrieveRestaurantDetails() {
    val id =
      requireNotNull(context.createdRestaurantId) { "Restaurant must be created before retrieval" }
    val result = helper.getRestaurantDetails(id)
    context.lastResult = result
    context.retrievedRestaurant = (result as? GetRestaurantResult.Success)?.restaurant
  }

  @Then("the restaurant details should match the created information")
  fun restaurantDetailsMatchCreatedInformation() {
    requireNotNull(context.createdRestaurantId) { "Restaurant ID should exist" }
    val restaurant =
      requireNotNull(context.retrievedRestaurant) { "Restaurant details should be retrievable" }

    restaurant.name.value shouldBe context.restaurantName
    restaurant.address.value shouldBe context.restaurantAddress
    restaurant.phone.value shouldBe context.restaurantPhone
    restaurant.email.value shouldBe context.restaurantEmail
  }

  // Scenario: Update restaurant information
  @And("I am the manager of the restaurant")
  fun iAmTheManagerOfTheRestaurant() {
    context.currentUserId = context.managerId ?: throw AssertionError(
      "Manager ID should be set after restaurant creation",
    )
  }

  @When(
    "I update the restaurant with name {string}, address {string}, phone {string}," +
      " and email {string}",
  )
  fun updateRestaurantWithFullDetails(name: String, address: String, phone: String, email: String) {
    context.restaurantName = name
    context.restaurantAddress = address
    context.restaurantPhone = phone
    context.restaurantEmail = email

    val id =
      requireNotNull(context.createdRestaurantId) { "Restaurant must be created before update" }

    val command = UpdateRestaurantCommand(
      restaurantId = id,
      managerId = context.currentUserId,
      name = name,
      address = address,
      phone = phone,
      email = email,
    )
    context.lastResult = helper.updateRestaurant(command)
  }

  @Then("the restaurant update should be successful")
  fun restaurantUpdateSuccessful() {
    context.lastResult should beInstanceOf<UpdateRestaurantResult.Success>()

    val id = requireNotNull(context.createdRestaurantId) { "Restaurant ID should exist" }
    val detailsResult = helper.getRestaurantDetails(id)
    context.retrievedRestaurant = (detailsResult as? GetRestaurantResult.Success)?.restaurant

    val restaurant =
      requireNotNull(
        context.retrievedRestaurant,
      ) { "Restaurant should be retrievable after update" }

    restaurant.name.value shouldBe context.restaurantName
    restaurant.address.value shouldBe context.restaurantAddress
    restaurant.phone.value shouldBe context.restaurantPhone
    restaurant.email.value shouldBe context.restaurantEmail
  }

  // Scenario: Delete restaurant
  @When("I delete the restaurant")
  fun deleteRestaurant() {
    val id =
      requireNotNull(context.createdRestaurantId) { "Restaurant must be created before deletion" }
    context.lastResult = helper.deleteRestaurant(id, context.currentUserId)
  }

  @Then("the restaurant should be deleted successfully")
  fun restaurantDeletedSuccessfully() {
    context.lastResult should beInstanceOf<DeleteRestaurantResult.Success>()

    val id = requireNotNull(context.createdRestaurantId) { "Restaurant ID should exist" }
    val deletedResult = helper.getRestaurantDetails(id)
    deletedResult shouldBe GetRestaurantResult.NotFound
  }
}
