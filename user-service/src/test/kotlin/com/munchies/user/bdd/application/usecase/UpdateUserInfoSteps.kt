package com.munchies.user.bdd.application.usecase

import com.munchies.user.application.port.inbound.UpdateUserInfo
import com.munchies.user.bdd.application.UserContext
import com.munchies.user.bdd.application.UserHelper
import com.munchies.user.domain.model.*
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.kotest.matchers.types.shouldBeInstanceOf
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@Singleton
class UpdateUserInfoSteps @Inject constructor(
  @Inject
  private val context: UserContext = UserContext(),
  @Inject
  private val helper: UserHelper,
  @Inject
  private val updateUserInfo: UpdateUserInfo,
) {
  private lateinit var userToUpdate: User

  @Given("^a update user info (.+) system$")
  fun a_update_user_info_system_status_system(systemStatus: String) {
    context.id = UserId()
    context.profile =
      UserProfile(
        "John Doe",
        Email("john.doe@example.com"),
        UserRole.CUSTOMER,
      )
    context.credentials = UserCredentials(context.id, "password", "salt")

    when (systemStatus) {
      "registered" -> {
        userToUpdate = helper.createUser(context)
        helper.saveUser(userToUpdate)
      }
      "not found" -> {
        userToUpdate = helper.createUser(context)
      }
      "invalid" -> {
        userToUpdate =
          mock {
            on { id } doReturn context.id
            on { profile } doReturn UserProfile("", Email(""), UserRole.CUSTOMER)
          }
        helper.saveUser(helper.createUser(context))
      }
    }
  }

  @When("I attempt to update a user info")
  fun i_attempt_to_update_a_user_info() {
    context.result = updateUserInfo.execute(userToUpdate)
  }

  @Then("^the system should (.+) to the changed user$")
  fun the_system_should_action_to_the_changed_user(action: String) {
    when (action) {
      "update" -> {
        context.result
          .shouldBeInstanceOf<UpdateUserInfo.Companion.UpdateUserInfoResult.Success>()
      }
      "not found" -> {
        context.result
          .shouldBeInstanceOf<UpdateUserInfo.Companion.UpdateUserInfoResult.UserNotFound>()
      }

      "fail" -> {
        context.result
          .shouldBeInstanceOf<UpdateUserInfo.Companion.UpdateUserInfoResult.Failure>()
      }
    }
  }
}
