package com.munchies.user.infrastructure.adapter.outbound.mongo.factory

import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.model.UserId
import com.munchies.user.infrastructure.adapter.outbound.mongo.document.UserCredentialsDocument

/**
 * Factory interface for converting between UserCredentials domain models and UserCredentialsDocument MongoDB documents.
 *
 * This interface provides methods to:
 * - Convert a UserCredentials domain model to a UserCredentialsDocument for persistence.
 * - Convert a UserCredentialsDocument to a UserCredentials domain model for application use.
 */
sealed interface UserCredentialsDocumentFactory {

  /**
   * Extension function to convert a UserCredentials domain model to a UserCredentialsDocument.
   *
   * @receiver The UserCredentials domain model to be converted.
   * @return The corresponding UserCredentialsDocument for persistence.
   */
  fun UserCredentials.toDocument(): UserCredentialsDocument

  /**
   * Extension function to convert a UserCredentialsDocument to a UserCredentials domain model.
   *
   * @receiver The UserCredentialsDocument to be converted.
   * @return The corresponding UserCredentials domain model.
   */
  fun UserCredentialsDocument.toDomain(): UserCredentials

  companion object {
    /**
     * Default implementation of the UserCredentialsDocumentFactory interface.
     *
     * This implementation provides the logic for converting between UserCredentials domain models
     * and UserCredentialsDocument MongoDB documents.
     */
    private class DefaultUserCredentialsDocumentFactory : UserCredentialsDocumentFactory {

      /**
       * Converts a UserCredentials domain model to a UserCredentialsDocument.
       *
       * @receiver The UserCredentials domain model to be converted.
       * @return The corresponding UserCredentialsDocument with mapped fields.
       */
      override fun UserCredentials.toDocument(): UserCredentialsDocument = UserCredentialsDocument(
        id = this.id.value,
        passwordHash = this.passwordHash,
        salt = this.salt,
        loginAttempts = this.loginAttempts,
        lockedUntil = this.lockedUntil,
        lastLogin = this.lastLogin,
      )

      /**
       * Converts a UserCredentialsDocument to a UserCredentials domain model.
       *
       * @receiver The UserCredentialsDocument to be converted.
       * @return The corresponding UserCredentials domain model with mapped fields.
       */
      override fun UserCredentialsDocument.toDomain(): UserCredentials = UserCredentials(
        id = UserId(this.id),
        passwordHash = this.passwordHash,
        salt = this.salt,
        loginAttempts = this.loginAttempts,
        lockedUntil = this.lockedUntil,
        lastLogin = this.lastLogin,
      )
    }

    /**
     * Singleton instance of the default UserCredentialsDocumentFactory implementation.
     *
     * This instance can be used to perform conversions without explicitly instantiating the factory.
     */
    val default: UserCredentialsDocumentFactory = DefaultUserCredentialsDocumentFactory()
  }
}
