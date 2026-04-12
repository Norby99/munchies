package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.RegisterUser
import com.munchies.user.application.port.inbound.RegisterUser.Companion.RegisterUserResult
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.port.UserCredentialsRepository
import com.munchies.user.domain.port.UserRepository

/**
 * Use case for registering a new user in the system.
 *
 * This class implements the `RegisterUser` interface and provides the logic for registering
 * a user by saving their details and credentials into the respective repositories.
 *
 * @property userRepository Repository for managing user entities.
 * @property credentialsRepository Repository for managing user credentials.
 */
class RegisterUserUseCase(
  private val userRepository: UserRepository,
  private val credentialsRepository: UserCredentialsRepository,
) : RegisterUser {

  private fun findUser(user: User): User? = userRepository.findById(user.id)
    ?: if (user.profile.email.isNotEmpty()) {
      userRepository.findByEmail(user.profile.email)
    } else if (user.profile.username.isNotEmpty()) {
      userRepository.findByUsername(user.profile.username)
    } else {
      null
    }

  /**
   * Executes the user registration process.
   *
   * This method checks if a user with the given ID already exists. If the user is not registered,
   * it saves the user and their credentials into the respective repositories. If an error occurs
   * during the process, it returns a failure result.
   *
   * @param user The user entity to be registered.
   * @param credentials The credentials associated with the user.
   * @return A `RegisterUserResult` indicating the outcome of the registration process:
   * - `UserIsAlreadyRegistered` if the user already exists.
   * - `Success` if the user and credentials are successfully saved.
   * - `Failure` if an error occurs during the process.
   */
  override fun execute(user: User, credentials: UserCredentials): RegisterUserResult {
    return findUser(user)
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
