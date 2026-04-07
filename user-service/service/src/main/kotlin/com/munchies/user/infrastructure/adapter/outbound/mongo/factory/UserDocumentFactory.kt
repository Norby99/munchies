package com.munchies.user.infrastructure.adapter.outbound.mongo.factory

import com.munchies.user.domain.factory.UserFactory
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserProfile
import com.munchies.user.domain.model.UserRole
import com.munchies.user.infrastructure.adapter.outbound.mongo.document.UserDocument

sealed interface UserDocumentFactory {
  fun User.toDocument(): UserDocument
  fun UserDocument.toDomain(): User

  companion object {
    private class DefaultUserDocumentFactory : UserDocumentFactory {
      override fun User.toDocument(): UserDocument = UserDocument(
        id = this.id.value,
        username = this.profile.username,
        email = this.profile.email,
        role = this.profile.role.toString(),
      )
      override fun UserDocument.toDomain(): User = UserFactory.default.create(
        id = this.id,
        profile = UserProfile(
          username = this.username,
          email = this.email,
          role = UserRole.run { this@toDomain.role.toUserRole() },
        ),
      )
    }

    val default: UserDocumentFactory = DefaultUserDocumentFactory()
  }
}
