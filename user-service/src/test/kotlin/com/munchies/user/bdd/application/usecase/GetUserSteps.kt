package com.munchies.user.bdd.application.usecase

import com.munchies.user.application.port.inbound.GetUser
import com.munchies.user.bdd.application.UserContext
import com.munchies.user.bdd.application.UserHelper
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.kotest.matchers.types.shouldBeInstanceOf
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class GetUserSteps @Inject constructor(
  @Inject
  private val context: UserContext = UserContext(),
  @Inject
  private val helper: UserHelper,
  @Inject
  private val getUser: GetUser,
) {

  @Given("a {word} user")
  fun aUserWithStatus(word: String) {
    this.context.id = helper.exampleId
    this.context.profile = helper.exampleUser.profile
    this.context.email = helper.exampleUser.profile.email.address
    this.context.credentials = helper.exampleCredendials

    when (word) {
      "registered" -> {
        this.helper.saveUser(helper.exampleUser)
      }
      else -> {}
    }
  }

  @When("I query the system with the user id")
  fun iQueryTheSystemWithTheUserId() {
    this.context.result = this.getUser.execute(this.context.id)
  }

  @Then("the system should respond with {word}")
  fun theSystemShouldRespondWithExpectedResponse(response: String) {
    // Implementation for asserting the expected response
    when (response) {
      "user_info" -> {
        this.context.result.shouldBeInstanceOf<GetUser.Companion.GetUserResult.Success>()
      }
      "not_found" -> {
        this.context.result.shouldBeInstanceOf<GetUser.Companion.GetUserResult.NotFound>()
      }
    }
  }
}
