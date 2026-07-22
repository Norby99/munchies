package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.UpdateUserInfo
import com.munchies.user.application.port.inbound.UpdateUserInfo.Companion.UpdateUserInfoResult
import com.munchies.user.application.port.inbound.UpdateUserInfo.Companion.UpdateUserInfoResult.Success
import com.munchies.user.application.port.inbound.UpdateUserInfo.Companion.UpdateUserInfoResult.UserNotFound
import com.munchies.user.domain.model.User
import com.munchies.user.domain.port.UserRepository

/**
 * Updates the persisted user profile information for an existing user.
 *
 * The use case validates that the user exists before rebuilding and saving the aggregate.
 */
class UpdateUserInfoUseCase(
  private val userRepository: UserRepository,
) : UpdateUserInfo {

  override fun execute(user: User): UpdateUserInfoResult {
    return userRepository.findById(user.id)?.let {
      return when (val newUser = User.factory.create(id = user.id.value, profile = user.profile)) {
        is User.Companion.UserFactory.UserFactoryResult.Success -> {
          userRepository.update(newUser.user)
          Success
        }
        is User.Companion.UserFactory.UserFactoryResult.Failure -> {
          UpdateUserInfoResult.Failure((newUser.reason))
        }
      }
    } ?: UserNotFound
  }
}
