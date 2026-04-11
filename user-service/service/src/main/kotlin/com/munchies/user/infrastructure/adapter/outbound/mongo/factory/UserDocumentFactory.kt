package com.munchies.user.infrastructure.adapter.outbound.mongo.factory

import com.munchies.user.domain.factory.UserFactory
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserProfile
import com.munchies.user.domain.model.UserRole
import com.munchies.user.infrastructure.adapter.outbound.mongo.document.UserDocument

/**
 * Factory interface for converting between the `User` domain model and the `UserDocument` MongoDB representation.
 *
 * This interface provides methods to:
 * - Convert a `User` domain model to a `UserDocument` for persistence.
 * - Convert a `UserDocument` back to a `User` domain model.
 */
sealed interface UserDocumentFactory {

  /**
   * Converts a `User` domain model to a `UserDocument`.
   *
   * @receiver The `User` domain model to be converted.
   * @return The corresponding `UserDocument` representation.
   */
  fun User.toDocument(): UserDocument

  /**
   * Converts a `UserDocument` to a `User` domain model.
   *
   * @receiver The `UserDocument` to be converted.
   * @return The corresponding `User` domain model.
   */
  fun UserDocument.toDomain(): User

  companion object {
    /**
     * Default implementation of the `UserDocumentFactory` interface.
     *
     * This implementation provides the logic for converting between `User` and `UserDocument`.
     */
    private class DefaultUserDocumentFactory : UserDocumentFactory {

      /**
       * Converts a `User` domain model to a `UserDocument`.
       *
       * @receiver The `User` domain model to be converted.
       * @return The corresponding `UserDocument` representation.
       */
      override fun User.toDocument(): UserDocument = UserDocument(
        id = this.id.value,
        username = this.profile.username,
        email = this.profile.email,
        role = this.profile.role.toString(),
      )

      /**
       * Converts a `UserDocument` to a `User` domain model.
       *
       * @receiver The `UserDocument` to be converted.
       * @return The corresponding `User` domain model.
       */
      override fun UserDocument.toDomain(): User = UserFactory.default.create(
        id = this.id,
        profile = UserProfile(
          username = this.username,
          email = this.email,
          role = UserRole.run { this@toDomain.role.toUserRole() },
        ),
      )
    }

    /**
     * Singleton instance of the default `UserDocumentFactory`.
     *
     * This instance can be used to perform conversions without explicitly instantiating the factory.
     */
    val default: UserDocumentFactory = DefaultUserDocumentFactory()
  }
}
