package com.munchies.user.domain.model

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.string.shouldNotBeEmpty
import org.junit.jupiter.api.Test

class UserFactoryTest {
  @Test
  fun `create should generate new id when id is empty`() {
    val profile = UserProfile("username", Email("email@example.com"), UserRole.CUSTOMER)

    val res = User.factory.create("", profile)

    val user = res.shouldBeSuccess()
    user.id.value.shouldNotBeEmpty()
  }

  @Test
  fun `create should use provided id when id is not empty`() {
    val id = "specific-id"

    val res = User.factory.create(id, exampleUserProfileRes)

    val user = res.shouldBeSuccess()
    user.id.value shouldBeEqual id
  }

  @Test
  fun `create should not create a user with empty username`() {
    val id = "specific-id"
    val res = User.factory.create(id, exampleUser.profile.copy(username = ""))

    val msg = res.shouldBeFailure()
    msg.shouldNotBeEmpty()
  }

  @Test
  fun `create should not create a user with empty email`() {
    val id = "specific-id"
    val res = User.factory.create(
      id,
      exampleUser.profile.copy(email = Email("")),
    )

    val msg = res.shouldBeFailure()
    msg.shouldNotBeEmpty()
  }

  @Test
  fun `create should not create a user with invalid role`() {
    val id = "specific-id"
    val res = User.factory.create(
      id,
      UserProfile.factory.create(
        username = exampleUserProfile.username,
        email = exampleUserProfile.email,
        "A really invalid role that will never be added :[",
      ),
    )

    val msg = res.shouldBeFailure()
    msg.shouldNotBeEmpty()
  }
}

// TODO same with UserProfileFactoryTest
