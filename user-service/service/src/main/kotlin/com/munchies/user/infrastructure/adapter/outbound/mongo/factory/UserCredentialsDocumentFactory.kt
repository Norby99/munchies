package com.munchies.user.infrastructure.adapter.outbound.mongo.factory

import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.model.UserId
import com.munchies.user.infrastructure.adapter.outbound.mongo.document.UserCredentialsDocument

sealed interface UserCredentialsDocumentFactory {
  fun UserCredentials.toDocument(): UserCredentialsDocument
  fun UserCredentialsDocument.toDomain(): UserCredentials

  companion object {
    private class DefaultUserCredentialsDocumentFactory : UserCredentialsDocumentFactory {
      override fun UserCredentials.toDocument(): UserCredentialsDocument = UserCredentialsDocument(
        id = this.id.value,
        passwordHash = this.passwordHash,
        salt = this.salt,
        loginAttempts = this.loginAttempts,
        lockedUntil = this.lockedUntil,
        lastLogin = this.lastLogin,
      )
      override fun UserCredentialsDocument.toDomain(): UserCredentials = UserCredentials(
        id = UserId(this.id),
        passwordHash = this.passwordHash,
        salt = this.salt,
        loginAttempts = this.loginAttempts,
        lockedUntil = this.lockedUntil,
        lastLogin = this.lastLogin,
      )
    }

    val default: UserCredentialsDocumentFactory = DefaultUserCredentialsDocumentFactory()
  }
}
