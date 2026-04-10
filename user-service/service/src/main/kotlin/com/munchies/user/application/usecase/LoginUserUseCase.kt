package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.LoginUser
import com.munchies.user.application.port.inbound.LoginUser.Companion.LoginResult
import com.munchies.user.application.port.inbound.LoginUser.Companion.LoginResult.Failure
import com.munchies.user.application.port.inbound.LoginUser.Companion.LoginResult.Success
import com.munchies.user.domain.model.User
import com.munchies.user.domain.port.PasswordHasher
import com.munchies.user.domain.port.UserCredentialsRepository
import com.munchies.user.domain.port.UserRepository

class LoginUserUseCase(
  private val userRepository: UserRepository,
  private val credentialsRepository: UserCredentialsRepository,
  private val passwordHasher: PasswordHasher,
) : LoginUser {

  private fun validateCredentials(user: User, providedPassword: String): Boolean {
    val credentials = credentialsRepository.findById(user.id) ?: return false
    val salt = credentials.salt
    val storedHash = credentials.passwordHash
    val providedHash = passwordHasher.hash(providedPassword, salt)
    return storedHash == providedHash
  }

  override fun execute(email: String, username: String, password: String): LoginResult {
    return when (email to username) {
      (email to "") ->
        userRepository.findByEmail(email)?.let { user ->
          if (validateCredentials(user, password)) {
            Success(user.id.value)
          } else {
            Failure
          }
        } ?: Failure
      ("" to username) ->
        userRepository.findByUsername(username)?.let { user ->
          if (validateCredentials(user, password)) {
            Success(user.id.value)
          } else {
            Failure
          }
        } ?: Failure
      else -> Failure
    }
  }
}
