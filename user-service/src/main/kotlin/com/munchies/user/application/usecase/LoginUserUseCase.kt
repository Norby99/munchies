package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.LoginUser
import com.munchies.user.application.port.inbound.LoginUser.Companion.LoginResult
import com.munchies.user.application.port.inbound.LoginUser.Companion.LoginResult.*
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.port.*

/**
 * Authenticates a user using either email or username and validates the provided password.
 *
 * The use case coordinates user lookup, credential retrieval, lockout checks,
 * and password verification while keeping transport concerns outside the application layer.
 */
class LoginUserUseCase(
  private val userRepository: UserRepository,
  private val credentialsRepository: UserCredentialsRepository,
  private val passwordHasher: PasswordHasher,
  private val timeProvider: TimeProvider,
) : LoginUser {

  private fun findUser(email: String, username: String): User? {
    println("email: $email, \t username: $username")
    return when {
      email.isNotBlank() -> userRepository.findByEmail(email)
      username.isNotBlank() -> userRepository.findByUsername(username)
      email.isEmpty().and(username.isEmpty()) -> null
      else -> null
    }
  }

  private fun authenticate(user: User, providedPassword: String): LoginResult =
    credentialsRepository.findById(user.id)
      ?.toLoginResult(user, providedPassword)
      ?: Failure

  private fun UserCredentials.toLoginResult(user: User, providedPassword: String): LoginResult =
    when {
      this.loginAttempts >= UserCredentials.MAXIMUM_LOGIN_ATTEMPTS -> LockedUser
      isBlocked(timeProvider()) -> BlockedLogin
      passwordHasher.hash(password = providedPassword, salt = salt) == passwordHash -> {
        credentialsRepository.resetLoginAttemps(user.id)
        Success(user.id.value, user.profile.role)
      }
      else -> {
        credentialsRepository.incrementLoginAttemps(user.id)
        credentialsRepository.update(this.copy(lockedUntil = timeProvider.addOneHour()()))
        Failure
      }
    }

  private fun UserCredentials.isBlocked(now: Long): Boolean = lockedUntil > now

  override fun execute(email: String, username: String, password: String): LoginResult =
    findUser(email = email.trim(), username = username.trim())
      ?.let { user -> authenticate(user, password) }
      ?: NotFound
}
