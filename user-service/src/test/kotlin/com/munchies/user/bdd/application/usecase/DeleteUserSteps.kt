package com.munchies.user.bdd.application.usecase

import com.munchies.user.application.port.inbound.DeleteUser
import com.munchies.user.bdd.application.UserContext
import com.munchies.user.bdd.application.UserHelper
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.model.update
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class DeleteUserSteps @Inject constructor(
  @Inject
  private val context: UserContext = UserContext(),
  @Inject
  private val helper: UserHelper,
  @Inject
  private val deleteUser: DeleteUser,
) {
  private lateinit var user: User

  @Given("^a delete (.+) system$")
  fun aDeleteSystem(systemStatus: String) {
    context.id = UserId()
    context.profile = helper.exampleUser.profile
    context.email = context.profile.email.address
    context.credentials = helper.exampleCredendials

    user = helper.exampleUser.update(context.id)

    when (systemStatus) {
      "registered" -> helper.saveUser(user)
      "not found" -> {}
    }
  }

  @When("I attempt to delete a user")
  fun iAttemptToDeleteAUser() {
    context.result = deleteUser.execute(context.id)
  }

  @Then("^the system should (.+) the user$")
  fun theSystemShouldActionTheUser(action: String) {
    when (action) {
      "delete" -> {
        context.result
          .shouldBeInstanceOf<
            DeleteUser.Companion.DeleteUserResult.Success,
            >()
        helper.userRepository.findById(context.id).shouldBeNull()
      }
      "not delete" -> {
        context.result
          .shouldBeInstanceOf<
            DeleteUser.Companion.DeleteUserResult.NotFound,
            >()
        helper.userRepository.findById(context.id).shouldBeNull()
      }
    }
  }
}
