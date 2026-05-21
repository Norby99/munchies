package com.munchies.user.infrastructure.adapter.dto.factory

import com.munchies.user.domain.factory.UserFactory
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserProfile
import com.munchies.user.domain.model.UserRole
import com.munchies.user.infrastructure.adapter.dto.UserDTO
import kotlin.run

/**
 * Factory interface for converting between User domain models and UserDTOs.
 *
 * This interface provides methods to map:
 * - A User domain model to a UserDTO.
 * - A UserDTO to a User domain model.
 */
sealed interface UserDTOFactory {

  /**
   * Extension function to convert a User domain model to a UserDTO.
   *
   * @receiver The User domain model to be converted.
   * @return The corresponding UserDTO.
   */
  fun User.fromDomain(): UserDTO

  /**
   * Extension function to convert a UserDTO to a User domain model.
   *
   * @receiver The UserDTO to be converted.
   * @return The corresponding User domain model.
   */
  fun UserDTO.fromDTO(): User

  companion object {
    /**
     * Default implementation of the UserDTOFactory interface.
     *
     * This implementation provides the logic for converting between User domain models and UserDTOs.
     */
    private class DefaultUserDTOFactory : UserDTOFactory {

      /**
       * Converts a User domain model to a UserDTO.
       *
       * @receiver The User domain model to be converted.
       * @return The corresponding UserDTO with mapped fields.
       */
      override fun User.fromDomain(): UserDTO = UserDTO(
        id = this.id.value,
        username = this.profile.username,
        email = this.profile.email,
        role = this.profile.role.toString(),
      )

      /**
       * Converts a UserDTO to a User domain model.
       *
       * @receiver The UserDTO to be converted.
       * @return The corresponding User domain model with mapped fields.
       */
      override fun UserDTO.fromDTO(): User = UserFactory.default.create(
        id = this.id,
        profile = UserProfile(
          username = this.username,
          email = this.email,
          role = UserRole.run { this@fromDTO.role.toUserRole() },
        ),
      )
    }

    /**
     * Singleton instance of the default UserDTOFactory implementation.
     *
     * This instance can be used to perform conversions without explicitly instantiating the factory.
     */
    val default: UserDTOFactory = DefaultUserDTOFactory()
  }
}
