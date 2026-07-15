package com.munchies.user.bdd.application.usecase

import com.munchies.user.application.port.inbound.RegisterUser
import com.munchies.user.bdd.application.UserContext
import com.munchies.user.bdd.application.UserHelper
import com.munchies.user.domain.model.*
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class RegisterUserSteps @Inject constructor(
  @Inject
  private val context: UserContext = UserContext(),
  @Inject
  private val helper: UserHelper,
  @Inject
  private val registerUser: RegisterUser,
) {

  @Given("a register {word} system")
  fun aSystemStatus(systemStatus: String) {
    context.id = UserId()
    context.profile = UserProfile("John Doe", Email("john.doe@example.com"), UserRole.CUSTOMER)
    context.credentials = UserCredentials(context.id, "password", "salt")

    when (systemStatus) {
      "blank" -> {}
      else -> {
        registerUser.execute(helper.createUser(context), context.credentials)
      }
    }
  }

  @When("I fill in the registration form with valid information")
  fun fillRegistrationForm() {
    context.result = registerUser.execute(helper.createUser(context), context.credentials)
  }

  @Then("^the system should (.+) a new user account$")
  fun createUserAccount(action: String) {
    when (action) {
      "create" -> {
        context.result.shouldBeInstanceOf<RegisterUser.Companion.RegisterUserResult.Success>()
        helper.userRepository.findById(context.id).shouldNotBeNull()
        helper.credentialsRepository.findById(context.id).shouldNotBeNull()
      }
      "not create" -> {
        context.result.shouldBeInstanceOf<
          RegisterUser.Companion.RegisterUserResult
            .UserIsAlreadyRegistered,
          >()
        helper.userRepository.findById(context.id).shouldNotBeNull()
        helper.credentialsRepository.findById(context.id).shouldNotBeNull()
      }
    }
  }
}
