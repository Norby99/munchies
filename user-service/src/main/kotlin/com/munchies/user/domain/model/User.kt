package com.munchies.user.domain.model

import com.munchies.commons.Entity
import com.munchies.commons.UUIDEntityId.Companion.newId
import com.munchies.user.domain.model.UserProfile.Companion.UserProfileFactory.UserProfileFactoryResult

/**
 * Represents a User in the domain model.
 *
 * @property id The unique identifier for the user, represented as a UserId.
 * @property profile The profile information associated with the user, represented as a UserProfile.
 */
class User private constructor(
  override val id: UserId,
  val profile: UserProfile,
) : Entity<UserId>(id = id) {

  fun updateEmailAsVerified(): User = User(this.id, this.profile.updateEmailAsVerified())

  companion object {
    /**
     * Factory interface for creating instances of the User domain model.
     * This interface provides a method to create a User object with a given ID and profile.
     */
    sealed interface UserFactory {
      sealed interface UserFactoryResult {
        data class Success(val user: User) : UserFactoryResult
        data class Failure(val reason: String) : UserFactoryResult
      }

      /**
       * Creates a new User instance.
       *
       * @param id The unique identifier for the user. If the ID is empty, a new UUID will be generated.
       * @param profile The profile information of the user. Defaults to an empty profile if not provided.
       * @return A new User instance with the specified ID and profile.
       */
      fun create(id: String, profile: UserProfile): UserFactoryResult
      fun create(id: String, profile: UserProfileFactoryResult): UserFactoryResult
    }

    /**
     * Default implementation of the UserFactory interface.
     * This implementation uses the UserId and User domain models to create User instances.
     */
    private class DefaultUserFactory : UserFactory {
      /**
       * Creates a new User instance with the specified ID and profile.
       * If the provided ID is empty, a new UUID is generated.
       *
       * @param id The unique identifier for the user.
       * @param profile The profile information of the user.
       * @return A new User instance.
       */
      override fun create(id: String, profile: UserProfile): UserFactory.UserFactoryResult =
        UserProfile.factory.create(
          username = profile.username,
          email = profile.email,
          role = profile.role,
        ).let {
          return when (it) {
            is UserProfileFactoryResult.Success ->
              UserFactory.UserFactoryResult.Success(
                User(id = UserId(id.ifEmpty { newId() }), profile),
              )
            is UserProfileFactoryResult.Failure ->
              UserFactory.UserFactoryResult.Failure(it.reason)
          }
        }

      override fun create(
        id: String,
        profile: UserProfileFactoryResult,
      ): UserFactory.UserFactoryResult = when (profile) {
        is UserProfileFactoryResult.Success -> this.create(id, profile.profile)
        is UserProfileFactoryResult.Failure -> UserFactory.UserFactoryResult.Failure(
          profile.reason,
        )
      }
    }

    /**
     * Singleton instance of the default UserFactory implementation.
     * This can be used to create User instances without explicitly instantiating the factory.
     */
    val factory: UserFactory = DefaultUserFactory()
  }
}
