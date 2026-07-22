package com.munchies.user.fixtures

import com.munchies.user.domain.model.Email
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.model.UserProfile
import com.munchies.user.domain.model.UserRole.Companion.toUserRole
import io.kotest.matchers.types.shouldBeInstanceOf

object UserFixtures {
  val exampleId = UserId("id")
  val exampleProfile = UserProfile("username", Email("email"), "CUSTOMER".toUserRole())
  val exampleUser = User.factory.create(exampleId.value, exampleProfile)
    .shouldBeInstanceOf<User.Companion.UserFactory.UserFactoryResult.Success>().user
}
