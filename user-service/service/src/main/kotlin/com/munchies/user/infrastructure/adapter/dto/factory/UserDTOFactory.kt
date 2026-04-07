package com.munchies.user.infrastructure.adapter.dto.factory

import com.munchies.user.domain.factory.UserFactory
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserProfile
import com.munchies.user.domain.model.UserRole
import com.munchies.user.infrastructure.adapter.dto.UserDTO
import kotlin.run

sealed interface UserDTOFactory {
  fun User.fromDomain(): UserDTO
  fun UserDTO.fromDTO(): User

  companion object {
    private class DefaultUserDTOFactory : UserDTOFactory {
      override fun User.fromDomain(): UserDTO = UserDTO(
        id = this.id.value,
        username = this.profile.username,
        email = this.profile.email,
        role = this.profile.role.toString(),
      )
      override fun UserDTO.fromDTO(): User = UserFactory.default.create(
        id = this.id,
        profile = UserProfile(
          username = this.username,
          email = this.email,
          role = UserRole.run { this@fromDTO.role.toUserRole() },
        ),
      )
    }

    val default: UserDTOFactory = DefaultUserDTOFactory()
  }
}
