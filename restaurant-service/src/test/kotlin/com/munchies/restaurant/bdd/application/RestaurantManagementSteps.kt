package com.munchies.restaurant.bdd.application

import com.munchies.restaurant.application.RestaurantApplicationService
import com.munchies.restaurant.application.usecases.CreateRestaurantCommand
import com.munchies.restaurant.application.usecases.CreateRestaurantResult
import com.munchies.restaurant.application.usecases.CreateRestaurantUseCase
import com.munchies.restaurant.application.usecases.DeleteRestaurantCommand
import com.munchies.restaurant.application.usecases.DeleteRestaurantResult
import com.munchies.restaurant.application.usecases.DeleteRestaurantUseCase
import com.munchies.restaurant.application.usecases.GetRestaurantCommand
import com.munchies.restaurant.application.usecases.GetRestaurantResult
import com.munchies.restaurant.application.usecases.GetRestaurantUseCase
import com.munchies.restaurant.application.usecases.UpdateRestaurantCommand
import com.munchies.restaurant.application.usecases.UpdateRestaurantResult
import com.munchies.restaurant.application.usecases.UpdateRestaurantUseCase
import com.munchies.restaurant.domain.entity.Restaurant
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.infrastructure.persistence.InMemoryRestaurantRepository
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions

/**
 * Step definitions for Restaurant Creation and Configuration BDD scenarios
 */
class RestaurantManagementSteps {

  private val restaurantRepository: RestaurantRepository by lazy { InMemoryRestaurantRepository() }

  private val restaurantApplicationService: RestaurantApplicationService by lazy {
    RestaurantApplicationService(
      createRestaurantUseCase = CreateRestaurantUseCase(restaurantRepository),
      updateRestaurantUseCase = UpdateRestaurantUseCase(restaurantRepository),
      getRestaurantUseCase = GetRestaurantUseCase(restaurantRepository),
      deleteRestaurantUseCase = DeleteRestaurantUseCase(restaurantRepository),
    )
  }

  private var currentUserId: String = "user-123"
  private var managerId: String? = null
  private var restaurantName: String? = null
  private var restaurantAddress: String? = null
  private var restaurantPhone: String? = null
  private var restaurantEmail: String? = null
  private var createdRestaurantId: String? = null
  private var lastCreationResult: CreateRestaurantResult? = null
  private var lastUpdateResult: UpdateRestaurantResult? = null
  private var lastDeleteResult: DeleteRestaurantResult? = null
  private var retrievedRestaurant: Restaurant? = null

  @Given(
    "I created a restaurant with name {string}, address {string}, phone {string}," +
      " and email {string}",
  )
  fun createdRestaurantWithAllDetails(name: String, address: String, phone: String, email: String) {
    createAndStoreRestaurant(name, address, phone, email)
  }

  @And("I am the manager of the restaurant")
  fun iAmTheManagerOfTheRestaurant() {
    currentUserId = managerId ?: throw AssertionError(
      "Manager ID should be set after restaurant creation",
    )
  }

  @When(
    "I create a restaurant with name {string}, address {string}, phone {string}," +
      " and email {string}",
  )
  fun createRestaurantWithFullDetails(name: String, address: String, phone: String, email: String) {
    createAndStoreRestaurant(name, address, phone, email)
  }

  @When("I retrieve the restaurant details")
  fun retrieveRestaurantDetails() {
    requireNotNull(createdRestaurantId) { "Restaurant must be created before retrieval" }
    val result = runBlocking {
      restaurantApplicationService.getRestaurantDetails(
        GetRestaurantCommand(createdRestaurantId!!),
      )
    }
    retrievedRestaurant = (result as? GetRestaurantResult.Success)?.restaurant
  }

  @When(
    "I update the restaurant with name {string}, address {string}, phone {string}," +
      " and email {string}",
  )
  fun updateRestaurantWithFullDetails(name: String, address: String, phone: String, email: String) {
    restaurantName = name
    restaurantAddress = address
    restaurantPhone = phone
    restaurantEmail = email

    requireNotNull(createdRestaurantId) { "Restaurant must be created before update" }

    val command = UpdateRestaurantCommand(
      restaurantId = createdRestaurantId!!,
      managerId = currentUserId,
      name = name,
      address = address,
      phone = phone,
      email = email,
    )
    lastUpdateResult = runBlocking { restaurantApplicationService.updateRestaurant(command) }
  }

  @When("I delete the restaurant")
  fun deleteRestaurant() {
    requireNotNull(createdRestaurantId) { "Restaurant must be created before deletion" }
    lastDeleteResult = runBlocking {
      val command = DeleteRestaurantCommand(createdRestaurantId!!, currentUserId)
      restaurantApplicationService.deleteRestaurant(command)
    }
  }

  @Then("the restaurant should be created successfully")
  fun restaurantCreatedSuccessfully() {
    val result = lastCreationResult as? CreateRestaurantResult.Success
      ?: throw AssertionError("Restaurant creation should be successful")

    createdRestaurantId = result.restaurantId
    Assertions.assertFalse(
      result.restaurantId.isBlank(),
      "Restaurant ID should not be blank",
    )
  }

  @Then("the restaurant details should match the created information")
  fun restaurantDetailsMatchCreatedInformation() {
    requireNotNull(createdRestaurantId) { "Restaurant ID should exist" }
    requireNotNull(retrievedRestaurant) { "Restaurant details should be retrievable" }

    with(retrievedRestaurant!!) {
      Assertions.assertEquals(restaurantName, name.value, "Restaurant name should match")
      Assertions.assertEquals(
        restaurantAddress,
        address.value,
        "Restaurant address should match",
      )
      Assertions.assertEquals(restaurantPhone, phone.value, "Restaurant phone should match")
      Assertions.assertEquals(restaurantEmail, email.value, "Restaurant email should match")
    }
  }

  @Then("the restaurant update should be successful")
  fun restaurantUpdateSuccessful() {
    Assertions.assertNotNull(lastUpdateResult, "Update result should not be null")
    Assertions.assertTrue(
      lastUpdateResult is UpdateRestaurantResult.Success,
      "Restaurant update should be successful",
    )

    retrievedRestaurant = runBlocking {
      val result = restaurantApplicationService.getRestaurantDetails(
        GetRestaurantCommand(createdRestaurantId!!),
      )
      (result as? GetRestaurantResult.Success)?.restaurant
    }

    with(
      retrievedRestaurant ?: throw AssertionError("Restaurant should be retrievable after update"),
    ) {
      Assertions.assertEquals(restaurantName, name.value, "Restaurant name should be updated")
      Assertions.assertEquals(
        restaurantAddress,
        address.value,
        "Restaurant address should be updated",
      )
      Assertions.assertEquals(
        restaurantPhone,
        phone.value,
        "Restaurant phone should be updated",
      )
      Assertions.assertEquals(
        restaurantEmail,
        email.value,
        "Restaurant email should be updated",
      )
    }
  }

  @Then("the restaurant should be deleted successfully")
  fun restaurantDeletedSuccessfully() {
    lastDeleteResult as? DeleteRestaurantResult.Success
      ?: throw AssertionError("Restaurant deletion should be successful")

    requireNotNull(createdRestaurantId) { "Restaurant ID should exist" }
    val deletedResult = runBlocking {
      restaurantApplicationService.getRestaurantDetails(
        GetRestaurantCommand(createdRestaurantId!!),
      )
    }
    Assertions.assertEquals(
      GetRestaurantResult.NotFound,
      deletedResult,
      "Deleted restaurant should not be retrievable",
    )
  }

  private fun createAndStoreRestaurant(
    name: String,
    address: String,
    phone: String,
    email: String,
  ) {
    restaurantName = name
    restaurantAddress = address
    restaurantPhone = phone
    restaurantEmail = email

    val command = CreateRestaurantCommand(
      managerId = currentUserId,
      name = name,
      address = address,
      phone = phone,
      email = email,
    )

    lastCreationResult = runBlocking { restaurantApplicationService.createRestaurant(command) }

    createdRestaurantId = (lastCreationResult as? CreateRestaurantResult.Success)?.restaurantId
      ?: throw AssertionError("Restaurant creation should be successful")
    managerId = currentUserId
  }
}
