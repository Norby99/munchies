package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.VerifyUserEmail
import com.munchies.user.application.port.inbound.VerifyUserEmail.Companion.VerifyUserEmailResult
import com.munchies.user.application.port.inbound.VerifyUserEmail.Companion.VerifyUserEmailResult.ConfirmedEmail
import com.munchies.user.application.port.inbound.VerifyUserEmail.Companion.VerifyUserEmailResult.InvalidRequest
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.port.PasswordHasher
import com.munchies.user.domain.port.UserRepository

class VerifyUserEmailUseCase(
  private val userRepository: UserRepository,
  private val hasher: PasswordHasher,
) : VerifyUserEmail {

  override fun execute(id: String, otk: String): VerifyUserEmailResult {
    val user = userRepository.findById(UserId(id)) ?: return InvalidRequest
    if (hasher.hash(user.id.value, user.profile.email.address) != otk) {
      return InvalidRequest
    }
    userRepository.update(
      user.updateEmailAsVerified(),
    )
    return ConfirmedEmail
  }
}
