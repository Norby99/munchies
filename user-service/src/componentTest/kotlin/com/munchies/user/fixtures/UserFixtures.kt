package com.munchies.user.fixtures

import com.munchies.user.domain.model.*
import io.kotest.matchers.types.shouldBeInstanceOf

object UserFixtures {
  val exampleId = UserId("id")
  val exampleProfile = UserProfile("username", Email("email"), UserRole.CUSTOMER)
  val exampleUser = User.factory.create(exampleId.value, exampleProfile)
    .shouldBeInstanceOf<User.Companion.UserFactory.UserFactoryResult.Success>().user
}
