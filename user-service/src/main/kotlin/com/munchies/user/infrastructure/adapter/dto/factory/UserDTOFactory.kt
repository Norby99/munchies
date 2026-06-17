package com.munchies.user.infrastructure.adapter.dto.factory

import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserProfile
import com.munchies.user.infrastructure.adapter.dto.UserDTO

/**
 * Factory interface for converting between User domain models and UserDTOs.
 *
 * This interface provides methods to map:
 * - A User domain model to a UserDTO.
 * - A UserDTO to a User domain model.
 */
object UserDTOFactory {
  sealed interface UserDTOFactoryResult {
    data class Success(val user: User) : UserDTOFactoryResult
    data class Failure(val reason: String) : UserDTOFactoryResult
  }

  /**
   * Converts a User domain model to a UserDTO.
   *
   * @receiver The User domain model to be converted.
   * @return The corresponding UserDTO with mapped fields.
   */
  fun User.toDTO(): UserDTO = UserDTO(
    id = this.id.value,
    username = this.profile.username,
    email = this.profile.email.address,
    role = this.profile.role.toString(),
  )

  /**
   * Converts a UserDTO to a User domain model.
   *
   * @receiver The UserDTO to be converted.
   * @return The corresponding User domain model with mapped fields.
   */
  fun UserDTO.toDomain(): UserDTOFactoryResult {
    return when (
      val profile = UserProfile.factory.create(
        this.username,
        this.email,
        this.role,
      )
    ) {
      is UserProfile.Companion.UserProfileFactory.UserProfileFactoryResult.Failure,
      -> UserDTOFactoryResult.Failure(profile.reason)
      is UserProfile.Companion.UserProfileFactory.UserProfileFactoryResult.Success -> {
        when (val user = User.factory.create(this.id, profile.profile)) {
          is User.Companion.UserFactory.UserFactoryResult.Failure,
          -> UserDTOFactoryResult.Failure(user.reason)
          is User.Companion.UserFactory.UserFactoryResult.Success -> {
            UserDTOFactoryResult.Success(user.user)
          }
        }
      }
    }
  }
}
