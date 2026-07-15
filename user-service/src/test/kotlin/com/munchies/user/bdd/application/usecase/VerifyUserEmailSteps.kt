package com.munchies.user.bdd.application.usecase

import com.munchies.user.application.port.inbound.VerifyUserEmail
import com.munchies.user.bdd.application.UserContext
import com.munchies.user.bdd.application.UserHelper
import com.munchies.user.domain.model.*
import com.munchies.user.domain.port.PasswordHasher
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class VerifyUserEmailSteps @Inject constructor(
  @Inject
  private val context: UserContext = UserContext(),
  @Inject
  private val helper: UserHelper,
  @Inject
  private val verifyUserEmail: VerifyUserEmail,
  @Inject
  private val passwordHasher: PasswordHasher,
) {
  private lateinit var user: User
  private var otk: String = ""

  @Given("^a verify email (.+) system$")
  fun aVerifyEmailSystem(systemStatus: String) {
    context.id = UserId()
    context.profile = helper.exampleUser.profile
    context.email = context.profile.email.address
    context.credentials = helper.exampleCredendials
    user = helper.exampleUser.update(context.id)
    otk = passwordHasher.hash(user.id.value, user.profile.email.address)

    when (systemStatus) {
      "registered" -> helper.saveUser(user)
      "not found" -> {}
    }
  }

  @When("I attempt to verify the user email with {word}")
  fun iAttemptToVerifyTheUserEmailWith(value: String) {
    val providedOtk = when (value) {
      "correct" -> otk
      else -> "wrong-otk"
    }

    context.result = verifyUserEmail.execute(user.id.value, providedOtk)
  }

  @Then("^the system should (.+) the email$")
  fun theSystemShouldActionTheEmail(action: String) {
    when (action) {
      "confirm" -> {
        context.result
          .shouldBeInstanceOf<
            VerifyUserEmail.Companion.VerifyUserEmailResult.ConfirmedEmail,
            >()
        helper.userRepository.findById(
          user.id,
        ).shouldNotBeNull().profile.email.isVerified.shouldBeTrue()
      }
      "invalid request" -> {
        context.result
          .shouldBeInstanceOf<
            VerifyUserEmail.Companion.VerifyUserEmailResult.InvalidRequest,
            >()
      }
    }
  }
}
