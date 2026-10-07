package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.LoginUser
import com.munchies.user.application.port.inbound.LoginUser.Companion.LoginResult
import com.munchies.user.application.port.inbound.LoginUser.Companion.LoginResult.*
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.port.*
import org.slf4j.LoggerFactory

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
      this.loginAttempts >= UserCredentials.MAXIMUM_LOGIN_ATTEMPTS -> {
        credentialsRepository.update(this.copy(lockedUntil = timeProvider.addOneHour()()))
        logger.warn("User {} locked after {} failed login attempts", user.id.value, loginAttempts)
        LockedUser
      }
      isBlocked(timeProvider()) -> {
        logger.warn("Login refused: user {} is temporarily blocked", user.id.value)
        BlockedLogin
      }
      passwordHasher.hash(password = providedPassword, salt = salt) == passwordHash -> {
        credentialsRepository.resetLoginAttemps(user.id)
        logger.info("User {} logged in", user.id.value)
        Success(user.id.value, user.profile.role)
      }
      else -> {
        credentialsRepository.incrementLoginAttemps(user.id)
        logger.warn("Failed login for user {}: wrong password", user.id.value)
        Failure
      }
    }

  private fun UserCredentials.isBlocked(now: Long): Boolean = lockedUntil > now

  override fun execute(email: String, username: String, password: String): LoginResult =
    findUser(email = email.trim(), username = username.trim())
      ?.let { user -> authenticate(user, password) }
      ?: NotFound

  private companion object {
    val logger = LoggerFactory.getLogger(LoginUserUseCase::class.java)
  }
}
