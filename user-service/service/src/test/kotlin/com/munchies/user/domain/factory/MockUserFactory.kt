package com.munchies.user.domain.factory

import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserProfile

class MockUserFactory {
  fun create(id: String = "", profile: UserProfile = UserProfile.empty): User =
    UserFactory.default.create(
      id,
      profile,
    )
}
