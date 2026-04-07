package com.munchies.user.domain.factory

import com.munchies.commons.UUIDEntityId.Companion.newId
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.model.UserProfile

interface UserFactory {
  fun create(id: String, profile: UserProfile = UserProfile.empty): User

  companion object {
    private class DefaultUserFactory : UserFactory {
      override fun create(id: String, profile: UserProfile): User =
        User(id = UserId(id.ifEmpty { newId() }), profile)
    }

    val default: UserFactory = DefaultUserFactory()
  }
}
