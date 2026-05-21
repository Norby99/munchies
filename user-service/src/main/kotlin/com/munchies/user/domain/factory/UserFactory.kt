package com.munchies.user.domain.factory

import com.munchies.commons.UUIDEntityId.Companion.newId
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.model.UserProfile

/**
 * Factory interface for creating instances of the User domain model.
 * This interface provides a method to create a User object with a given ID and profile.
 */
interface UserFactory {

  /**
   * Creates a new User instance.
   *
   * @param id The unique identifier for the user. If the ID is empty, a new UUID will be generated.
   * @param profile The profile information of the user. Defaults to an empty profile if not provided.
   * @return A new User instance with the specified ID and profile.
   */
  fun create(id: String, profile: UserProfile = UserProfile.empty): User

  companion object {
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
      override fun create(id: String, profile: UserProfile): User =
        User(id = UserId(id.ifEmpty { newId() }), profile)
    }

    /**
     * Singleton instance of the default UserFactory implementation.
     * This can be used to create User instances without explicitly instantiating the factory.
     */
    val default: UserFactory = DefaultUserFactory()
  }
}
