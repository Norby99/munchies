package com.munchies.user.infrastructure.adapter.inbound.web.config

import com.munchies.user.application.port.inbound.*
import com.munchies.user.application.usecase.*
import com.munchies.user.domain.port.*
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

/**
 * Micronaut factory that wires application use cases to their concrete
 * infrastructure dependencies.
 *
 * Each method is annotated with @Singleton so the returned use case instance
 * will be managed by the Micronaut context.
 */
@Factory
class UserBeans {

  @Singleton
  fun getUser(repo: UserRepository): GetUser = GetUserUseCase(repo)

  @Singleton
  fun registerUser(
    userRepository: UserRepository,
    userCredentialsRepository: UserCredentialsRepository,
    hasher: PasswordHasher,
    mailer: Mailer,
  ): RegisterUser = RegisterUserUseCase(userRepository, userCredentialsRepository, hasher, mailer)

  @Singleton
  fun loginUser(
    userRepository: UserRepository,
    userCredentialsRepository: UserCredentialsRepository,
    passwordHasher: PasswordHasher,
  ): LoginUser = LoginUserUseCase(
    userRepository,
    userCredentialsRepository,
    passwordHasher,
    defaultTimeProvider(),
  )

  @Singleton
  fun updateUserPassword(
    userRepository: UserRepository,
    userCredentialsRepository: UserCredentialsRepository,
    passwordHasher: PasswordHasher,
  ): UpdateUserPassword = UpdateUserPasswordUseCase(
    userRepository,
    userCredentialsRepository,
    passwordHasher,
    defaultTimeProvider(),
  )

  @Singleton
  fun updateUserInfo(userRepository: UserRepository): UpdateUserInfo =
    UpdateUserInfoUseCase(userRepository)

  @Singleton
  fun deleteUser(userRepository: UserRepository): DeleteUser = DeleteUserUseCase(userRepository)

  @Singleton
  fun verifyEmail(userRepository: UserRepository, hasher: PasswordHasher): VerifyUserEmail =
    VerifyUserEmailUseCase(userRepository, hasher)

  @Singleton
  fun getUserServices(
    getUser: GetUser,
    registerUser: RegisterUser,
    loginUser: LoginUser,
    updateUserPassword: UpdateUserPassword,
    updateUserInfo: UpdateUserInfo,
    deleteUser: DeleteUser,
    verifyUserEmail: VerifyUserEmail,
  ) = UserServices(
    getUser = getUser,
    registerUser = registerUser,
    loginUser = loginUser,
    updateUserPassword = updateUserPassword,
    updateUserInfo = updateUserInfo,
    deleteUser = deleteUser,
    verifyUserEmail = verifyUserEmail,
  )
}

data class UserServices(
  val getUser: GetUser,
  val registerUser: RegisterUser,
  val loginUser: LoginUser,
  val updateUserPassword: UpdateUserPassword,
  val updateUserInfo: UpdateUserInfo,
  val deleteUser: DeleteUser,
  val verifyUserEmail: VerifyUserEmail,
)
