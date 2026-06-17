package com.munchies.user.infrastructure.adapter.outbound.mongo.factory

import com.munchies.user.domain.model.Email
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserProfile
import com.munchies.user.infrastructure.adapter.outbound.mongo.document.UserDocument

/**
 * Factory interface for converting between the `User` domain model and the `UserDocument` MongoDB representation.
 *
 * This interface provides methods to:
 * - Convert a `User` domain model to a `UserDocument` for persistence.
 * - Convert a `UserDocument` back to a `User` domain model.
 */
object UserDocumentFactory {

  sealed interface UserDocumentFactoryResult {
    data class Success(val user: User) : UserDocumentFactoryResult
    data class Failure(val reason: String) : UserDocumentFactoryResult
  }

  /**
   * Converts a `User` domain model to a `UserDocument`.
   *
   * @receiver The `User` domain model to be converted.
   * @return The corresponding `UserDocument` representation.
   */
  fun User.toDocument(): UserDocument = UserDocument(
    id = this.id.value,
    username = this.profile.username,
    email = this.profile.email.address,
    isVerified = this.profile.email.isVerified,
    role = this.profile.role.toString(),
  )

  /**
   * Converts a `UserDocument` to a `User` domain model.
   *
   * @receiver The `UserDocument` to be converted.
   * @return The corresponding `User` domain model.
   */
  fun UserDocument.toDomain(): UserDocumentFactoryResult {
    return when (
      val profile = UserProfile.factory.create(
        this.username,
        Email(this.email, this.isVerified),
        this.role,
      )
    ) {
      is UserProfile.Companion.UserProfileFactory.UserProfileFactoryResult.Failure ->
        UserDocumentFactoryResult.Failure(profile.reason)
      is UserProfile.Companion.UserProfileFactory.UserProfileFactoryResult.Success -> {
        when (val user = User.factory.create(this.id, profile.profile)) {
          is User.Companion.UserFactory.UserFactoryResult.Failure ->
            UserDocumentFactoryResult.Failure(user.reason)
          is User.Companion.UserFactory.UserFactoryResult.Success -> {
            UserDocumentFactoryResult.Success(user.user)
          }
        }
      }
    }
  }

  fun UserDocument.toNullableDomain(): User? = when (val user = this.toDomain()) {
    is UserDocumentFactoryResult.Success -> user.user
    else -> null
  }
}
