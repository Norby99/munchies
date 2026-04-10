package com.munchies.user.domain.factory

import com.munchies.user.domain.model.UserProfile
import com.munchies.user.domain.model.UserRole
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class UserFactoryTest {
  @Test
  fun `create should generate new id when id is empty`() {
    val profile = UserProfile("username", "email@example.com", UserRole.CUSTOMER)

    val user = UserFactory.default.create("", profile)

    user.id.value shouldNotBe null
    profile shouldBeEqual user.profile
  }

  @Test
  fun `create should use provided id when id is not empty`() {
    val id = "specific-id"
    val profile = UserProfile("username", "email@example.com", UserRole.CUSTOMER)

    val user = UserFactory.default.create(id, profile)

    id shouldBeEqual user.id.value
    profile shouldBeEqual user.profile
  }

  @Test
  fun createShouldUseEmptyProfileWhenProfileIsNotProvided() {
    val id = "specific-id"

    val user = UserFactory.default.create(id)

    id shouldBeEqual user.id.value
    UserProfile.empty shouldBeEqual user.profile
  }
}
