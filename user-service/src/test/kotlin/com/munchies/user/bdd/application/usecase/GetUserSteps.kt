package com.munchies.user.bdd.application.usecase

import com.munchies.user.bdd.application.UserContext
import com.munchies.user.bdd.application.UserHelper
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class GetUserSteps @Inject constructor(
  @Inject
  private val context: UserContext = UserContext(),
  @Inject
  private val helper: UserHelper
) {


  @Given("a registered user")
  fun aRegisteredUser() {
    // Implementation for setting up a registered user
  }


  @When("I query the system with the user id")
  fun iQueryTheSystemWithTheUserId() {
    // Implementation for querying the system with the user id
  }


  @Then("the system should respond with the user information")
  fun theSystemShouldRespondWithTheUserInfo() {
    // Implementation for checking the system's response
  }
}
