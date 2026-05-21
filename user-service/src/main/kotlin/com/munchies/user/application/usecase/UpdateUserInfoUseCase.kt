package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.UpdateUserInfo
import com.munchies.user.application.port.inbound.UpdateUserInfo.Companion.UpdateUserInfoResult
import com.munchies.user.application.port.inbound.UpdateUserInfo.Companion.UpdateUserInfoResult.Success
import com.munchies.user.application.port.inbound.UpdateUserInfo.Companion.UpdateUserInfoResult.UserNotFound
import com.munchies.user.domain.model.User
import com.munchies.user.domain.port.UserRepository

class UpdateUserInfoUseCase(
  private val userRepository: UserRepository,
) : UpdateUserInfo {

  override fun execute(user: User): UpdateUserInfoResult {
    return userRepository.findById(user.id)?.let {
      userRepository.save(user.copy(profile = user.profile))
      Success
    } ?: UserNotFound
  }
}
