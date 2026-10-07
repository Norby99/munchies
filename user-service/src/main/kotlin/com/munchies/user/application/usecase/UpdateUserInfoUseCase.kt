package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.UpdateUserInfo
import com.munchies.user.application.port.inbound.UpdateUserInfo.Companion.UpdateUserInfoResult
import com.munchies.user.application.port.inbound.UpdateUserInfo.Companion.UpdateUserInfoResult.Success
import com.munchies.user.application.port.inbound.UpdateUserInfo.Companion.UpdateUserInfoResult.UserNotFound
import com.munchies.user.domain.model.User
import com.munchies.user.domain.port.UserRepository
import org.slf4j.LoggerFactory

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
      val profile = user.profile.copy(role = it.profile.role)
      return when (val newUser = User.factory.create(id = user.id.value, profile = profile)) {
        is User.Companion.UserFactory.UserFactoryResult.Success -> {
          userRepository.update(newUser.user)
          logger.info("User {} updated their profile", user.id.value)
          Success
        }
        is User.Companion.UserFactory.UserFactoryResult.Failure -> {
          UpdateUserInfoResult.Failure((newUser.reason))
        }
      }
    } ?: UserNotFound
  }

  private companion object {
    val logger = LoggerFactory.getLogger(UpdateUserInfoUseCase::class.java)
  }
}
