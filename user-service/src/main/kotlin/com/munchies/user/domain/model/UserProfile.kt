package com.munchies.user.domain.model

import com.munchies.user.domain.model.UserProfile.Companion.UserProfileFactory.UserProfileFactoryResult
import com.munchies.user.domain.model.UserProfile.Companion.UserProfileFactory.UserProfileFactoryResult.Success
import com.munchies.user.domain.model.UserRole.Companion.toUserRole

/**
 * Represents the profile information of a user in the domain model.
 *
 * @property username The username of the user. This is a unique identifier for the user within the system.
 * @property email The email address of the user. Used for communication and account recovery.
 * @property role The role of the user, represented as a UserRole. Defines the user's permissions and access level.
 */
data class UserProfile(
  val username: String,
  val email: Email,
  val role: UserRole,
) {
  fun updateEmailAsVerified(): UserProfile = this.copy(email = this.email.copy(isVerified = true))

  companion object {

    sealed interface UserProfileFactory {
      sealed interface UserProfileFactoryResult {
        data class Success(val profile: UserProfile) : UserProfileFactoryResult
        data class Failure(val reason: String) : UserProfileFactoryResult
      }

      fun create(username: String, email: Email, role: UserRole): UserProfileFactoryResult
      fun create(username: String, email: Email, role: String): UserProfileFactoryResult
      fun create(username: String, email: String, role: String): UserProfileFactoryResult
    }

    private class DefaultUserProfileFactory : UserProfileFactory {
      override fun create(
        username: String,
        email: Email,
        role: UserRole,
      ): UserProfileFactoryResult {
        if (username.isEmpty()) return UserProfileFactoryResult.Failure("Username is empty")
        if (email.address.isEmpty()) {
          return UserProfileFactoryResult.Failure(
            "Email address is empty",
          )
        }
        return Success(UserProfile(username = username, email = email, role = role))
      }

      override fun create(username: String, email: Email, role: String): UserProfileFactoryResult {
        return try {
          this.create(username = username, email = email, role = role.toUserRole())
        } catch (e: IllegalArgumentException) {
          UserProfileFactoryResult.Failure(e.localizedMessage)
        }
      }

      override fun create(username: String, email: String, role: String): UserProfileFactoryResult =
        this.create(username = username, email = Email(email), role = role)
    }

    val factory: UserProfileFactory = DefaultUserProfileFactory()
  }
}
