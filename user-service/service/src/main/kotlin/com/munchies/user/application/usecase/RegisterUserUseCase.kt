package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.RegisterUser
import com.munchies.user.application.port.inbound.RegisterUser.Companion.RegisterUserResult
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.port.UserCredentialsRepository
import com.munchies.user.domain.port.UserRepository

class RegisterUserUseCase(
  private val userRepository: UserRepository,
  private val credentialsRepository: UserCredentialsRepository,
) : RegisterUser {
  override fun execute(user: User, credentials: UserCredentials): RegisterUserResult {
    return userRepository
      .findById(user.id)
      ?.let { RegisterUserResult.UserIsAlreadyRegistered }
      ?: try {
        userRepository.save(user)
        credentialsRepository.save(credentials.copy(id = user.id))
        RegisterUserResult.Success(user)
      } catch (e: kotlin.Error) {
        RegisterUserResult.Failure(e.localizedMessage)
      }
  }
}
