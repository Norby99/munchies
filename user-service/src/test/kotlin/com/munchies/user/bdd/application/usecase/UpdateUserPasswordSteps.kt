package com.munchies.user.bdd.application.usecase

import com.munchies.user.application.port.inbound.UpdateUserPassword
import com.munchies.user.bdd.application.UserContext
import com.munchies.user.bdd.application.UserHelper
import com.munchies.user.domain.model.*
import com.munchies.user.domain.port.PasswordHasher
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class UpdateUserPasswordSteps @Inject constructor(
  @Inject
  private val context: UserContext = UserContext(),
  @Inject
  private val helper: UserHelper,
  @Inject
  private val updateUserPassword: UpdateUserPassword,
  @Inject
  private val passwordHasher: PasswordHasher,
) {
  private lateinit var storedUser: User
  private lateinit var storedCredentials: UserCredentials
  private var requestId: String = ""

  @Given("^an update password (.+) system$")
  fun anUpdatePasswordSystem(systemStatus: String) {
    context.profile = helper.exampleUser.profile
    context.credentials = helper.exampleCredendials
    storedUser = helper.exampleUser.update(UserId())
    storedCredentials =
      UserCredentials(
        id = storedUser.id,
        passwordHash = passwordHasher.hash(
          context.credentials.passwordHash,
          context.credentials.salt,
        ),
        salt = context.credentials.salt,
      )

    requestId = storedUser.id.value

    when (systemStatus) {
      "registered" -> {
        helper.saveUser(storedUser)
        helper.saveCredentials(storedCredentials)
      }
      "blocked" -> {
        helper.saveUser(storedUser)
        helper.saveCredentials(storedCredentials.copy(lockedUntil = Long.MAX_VALUE))
      }
      "unauthorized" -> {
        helper.saveUser(storedUser)
        helper.saveCredentials(storedCredentials)
        requestId = UserId().value
      }
      "not found" -> {}
    }
  }

  @When("I attempt to update the user password with {word}")
  fun iAttemptToUpdateTheUserPasswordWith(credentials: String) {
    val oldPassword = when (credentials) {
      "correct" -> context.credentials.passwordHash
      else -> "wrong password"
    }

    context.result =
      updateUserPassword.execute(
        requestId,
        storedUser.profile.username,
        storedUser.profile.email.address,
        oldPassword,
        "new-password",
      )
  }

  @Then("^the system should (.+) the password$")
  fun theSystemShouldActionThePassword(action: String) {
    when (action) {
      "update" -> {
        context.result
          .shouldBeInstanceOf<
            UpdateUserPassword.Companion.UpdateUserPasswordResult.Success,
            >()
        helper.credentialsRepository.findById(storedUser.id).shouldNotBeNull()
      }
      "wrong credentials" -> {
        context.result
          .shouldBeInstanceOf<
            UpdateUserPassword.Companion.UpdateUserPasswordResult.WrongCredentials,
            >()
        helper.credentialsRepository.findById(storedUser.id).shouldNotBeNull()
      }
      "locked" -> {
        context.result
          .shouldBeInstanceOf<
            UpdateUserPassword.Companion.UpdateUserPasswordResult.LockedUser,
            >()
        helper.credentialsRepository.findById(storedUser.id).shouldNotBeNull()
      }
      "unauthorized" -> {
        context.result
          .shouldBeInstanceOf<
            UpdateUserPassword.Companion.UpdateUserPasswordResult.UnauthorizedOperation,
            >()
        helper.credentialsRepository.findById(storedUser.id).shouldNotBeNull()
      }
      "not found" -> {
        context.result
          .shouldBeInstanceOf<
            UpdateUserPassword.Companion.UpdateUserPasswordResult.UserNotFound,
            >()
      }
    }
  }
}
