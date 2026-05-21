package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.UpdateUserPassword
import com.munchies.user.application.port.inbound.UpdateUserPassword.Companion.UpdateUserPasswordResult
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.port.PasswordHasher
import com.munchies.user.domain.port.TimeProvider
import com.munchies.user.domain.port.UserCredentialsRepository
import com.munchies.user.domain.port.UserRepository
import com.munchies.user.domain.port.addOneHour

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
      loginAttempts = 0,
    )
    credentialsRepository.update(updatedCredentials)
  }

  private fun validateAndUpdatePassword(
    credentials: UserCredentials,
    oldPassword: String,
    newPassword: String,
  ): UpdateUserPasswordResult {
    return when {
      credentials.lockedUntil > timeProvider() -> UpdateUserPasswordResult.LockedUser
      passwordHasher
        .hash(oldPassword, credentials.salt) != credentials.passwordHash -> {
        val updatedCredentials = credentials.copy(
          lockedUntil = timeProvider.addOneHour(),
          loginAttempts = credentials.loginAttempts + 1,
        )
        credentialsRepository.update(updatedCredentials)
        UpdateUserPasswordResult.WrongCredentials
      }
      else -> {
        updatePassword(credentials, newPassword)
        UpdateUserPasswordResult.Success
      }
    }
  }

  override fun execute(
    user: User,
    oldPassword: String,
    newPassword: String,
  ): UpdateUserPasswordResult {
    return when (
      val credentials = findUser(email = user.profile.email, username = user.profile.username)
        ?.let { credentialsRepository.findById(it.id) }
    ) {
      null -> UpdateUserPasswordResult.UserNotFound

      else -> validateAndUpdatePassword(credentials, oldPassword, newPassword)
    }
  }
}
