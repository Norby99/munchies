package com.munchies.user.bdd.application.usecase

import com.munchies.user.application.port.inbound.LoginUser
import com.munchies.user.bdd.application.UserContext
import com.munchies.user.bdd.application.UserHelper
import com.munchies.user.domain.model.*
import com.munchies.user.domain.port.PasswordHasher
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.kotest.matchers.types.shouldBeInstanceOf
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class LoginUserSteps @Inject constructor(
  @Inject
  private val context: UserContext = UserContext(),
  @Inject
  private val helper: UserHelper,
  @Inject
  private val loginUser: LoginUser,
  @Inject
  private val passwordHasher: PasswordHasher,
) {
  @Given("^a login (.+) system$")
  fun aSystem(systemStatus: String) {
    context.id = UserId()
    context.profile = UserProfile("John Doe", Email("john.doe@example.com"), UserRole.CUSTOMER)
    context.credentials = UserCredentials(context.id, "password", "salt")
    val user = helper.createUser(context)
    val storedCredentials =
      context.credentials.copy(
        passwordHash = passwordHasher.hash(
          context.credentials.passwordHash,
          context.credentials.salt,
        ),
      )

    when (systemStatus) {
      "registered" -> {
        helper.saveUser(user)
        helper.saveCredentials(storedCredentials)
      }
      "not found" -> {}
      "blocked" -> {
        helper.saveUser(user)
        helper.saveCredentials(
          storedCredentials.copy(
            lockedUntil = Long.MAX_VALUE,
          ),
        )
      }
    }
  }

  @When("I attempt to login with {word}.")
  fun iAttemptToLoginWith(credentials: String) {
    val password = when (credentials) {
      "correct" -> "password"
      else -> "wrong password"
    }

    context.result =
      loginUser.execute(
        email = context.profile.email.address,
        username = context.profile.username,
        password = password,
      )
  }

  @Then("^the login system should respond with (.+)\\.$")
  fun theSystemShouldRespondWith(expectedResponse: String) {
    when (expectedResponse) {
      "login" ->
        context.result.shouldBeInstanceOf<LoginUser.Companion.LoginResult.Success>()
      "failure" ->
        context.result.shouldBeInstanceOf<LoginUser.Companion.LoginResult.Failure>()
      "blocked" ->
        context.result.shouldBeInstanceOf<LoginUser.Companion.LoginResult.BlockedLogin>()
    }
  }
}
