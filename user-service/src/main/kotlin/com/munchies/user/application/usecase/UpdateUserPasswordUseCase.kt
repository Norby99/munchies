package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.UpdateUserPassword
import com.munchies.user.application.port.inbound.UpdateUserPassword.Companion.UpdateUserPasswordResult
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.port.*

/**
 * Updates a user's password after validating identity, lockout state, and the current password.
 *
 * The use case also resets credential counters and lock metadata when the password update succeeds.
 */
class UpdateUserPasswordUseCase(
  private val userRepository: UserRepository,
  private val credentialsRepository: UserCredentialsRepository,
  private val passwordHasher: PasswordHasher,
  private val timeProvider: TimeProvider,
) :
  UpdateUserPassword {

  private fun findUser(email: String, username: String): User? = when {
    email.isNotBlank() -> userRepository.findByEmail(email)
    username.isNotBlank() -> userRepository.findByUsername(username)
    else -> null
  }

  private fun updatePassword(credentials: UserCredentials, newPassword: String) {
    val newSalt = passwordHasher.generateSalt()
    val newHash = passwordHasher.hash(password = newPassword, salt = newSalt)
    val updatedCredentials = UserCredentials(
      id = credentials.id,
      passwordHash = newHash,
      salt = newSalt,
      lockedUntil = 0L,
      lastLogin = timeProvider.invoke(),
      loginAttempts = 0,
    )
    credentialsRepository.update(updatedCredentials)
  }

  private fun validateAndUpdatePassword(
    providedId: String,
    credentials: UserCredentials,
    oldPassword: String,
    newPassword: String,
  ): UpdateUserPasswordResult {
    return when {
      credentials.lockedUntil > timeProvider() -> UpdateUserPasswordResult.LockedUser
      (
        passwordHasher
          .hash(oldPassword, credentials.salt) != credentials.passwordHash
        ) -> {
        val updatedCredentials = credentials.copy(
          lockedUntil = timeProvider.addOneHour()(),
          loginAttempts = credentials.loginAttempts + 1,
        )
        credentialsRepository.update(updatedCredentials)
        UpdateUserPasswordResult.WrongCredentials
      }
      (providedId != credentials.id.value) -> {
        UpdateUserPasswordResult.UnauthorizedOperation
      }
      else -> {
        updatePassword(credentials, newPassword)
        UpdateUserPasswordResult.Success
      }
    }
  }

  override fun execute(
    id: String,
    username: String,
    email: String,
    oldPassword: String,
    newPassword: String,
  ): UpdateUserPasswordResult {
    return when (
      val credentials = findUser(
        email = email,
        username = username,
      )
        ?.let { credentialsRepository.findById(it.id) }
    ) {
      null -> UpdateUserPasswordResult.UserNotFound

      else -> validateAndUpdatePassword(id, credentials, oldPassword, newPassword)
    }
  }
}
