package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.LoginUser
import com.munchies.user.application.port.inbound.LoginUser.Companion.LoginResult
import com.munchies.user.application.port.inbound.LoginUser.Companion.LoginResult.BlockedLogin
import com.munchies.user.application.port.inbound.LoginUser.Companion.LoginResult.Failure
import com.munchies.user.application.port.inbound.LoginUser.Companion.LoginResult.Success
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.port.PasswordHasher
import com.munchies.user.domain.port.TimeProvider
import com.munchies.user.domain.port.UserCredentialsRepository
import com.munchies.user.domain.port.UserRepository

class LoginUserUseCase(
  private val userRepository: UserRepository,
  private val credentialsRepository: UserCredentialsRepository,
  private val passwordHasher: PasswordHasher,
  private val timeProvider: TimeProvider,
) : LoginUser {

  private fun findUser(email: String, username: String): User? = when {
    email.isNotBlank() && username.isBlank() -> userRepository.findByEmail(email)
    username.isNotBlank() && email.isBlank() -> userRepository.findByUsername(username)
    else -> null
  }

  private fun authenticate(user: User, providedPassword: String): LoginResult =
    credentialsRepository.findById(user.id)
      ?.toLoginResult(user, providedPassword)
      ?: Failure

  private fun UserCredentials.toLoginResult(user: User, providedPassword: String): LoginResult =
    when {
      isBlocked(timeProvider()) -> BlockedLogin
      passwordHasher.hash(password = providedPassword, salt = salt) == passwordHash ->
        Success(user.id.value)
      else -> Failure
    }

  private fun UserCredentials.isBlocked(now: Long): Boolean = lockedUntil > now

  override fun execute(email: String, username: String, password: String): LoginResult =
    findUser(email = email.trim(), username = username.trim())
      ?.let { user -> authenticate(user, password) }
      ?: Failure
}
