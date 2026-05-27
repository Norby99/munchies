package com.munchies.user.infrastructure.adapter.inbound.web.config

import com.munchies.user.application.port.inbound.*
import com.munchies.user.application.usecase.*
import com.munchies.user.domain.port.PasswordHasher
import com.munchies.user.domain.port.UserCredentialsRepository
import com.munchies.user.domain.port.UserRepository
import com.munchies.user.domain.port.defaultTimeProvider
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

@Factory
class UserBeans {

  @Singleton
  fun getUser(repo: UserRepository): GetUser = GetUserUseCase(repo)

  @Singleton
  fun registerUser(
    userRepository: UserRepository,
    userCredentialsRepository: UserCredentialsRepository,
  ): RegisterUser = RegisterUserUseCase(userRepository, userCredentialsRepository)

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
  fun getUserServices(
    getUser: GetUser,
    registerUser: RegisterUser,
    loginUser: LoginUser,
    updateUserPassword: UpdateUserPassword,
    updateUserInfo: UpdateUserInfo,
    deleteUser: DeleteUser,
  ) = UserServices(
    getUser = getUser,
    registerUser = registerUser,
    loginUser = loginUser,
    updateUserPassword = updateUserPassword,
    updateUserInfo = updateUserInfo,
    deleteUser = deleteUser,
  )
}

data class UserServices(
  val getUser: GetUser,
  val registerUser: RegisterUser,
  val loginUser: LoginUser,
  val updateUserPassword: UpdateUserPassword,
  val updateUserInfo: UpdateUserInfo,
  val deleteUser: DeleteUser,
)
