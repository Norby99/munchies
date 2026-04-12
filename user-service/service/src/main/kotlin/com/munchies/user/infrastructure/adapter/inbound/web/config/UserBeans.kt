package com.munchies.user.infrastructure.adapter.inbound.web.config

import com.munchies.user.application.port.inbound.GetUser
import com.munchies.user.application.port.inbound.LoginUser
import com.munchies.user.application.port.inbound.RegisterUser
import com.munchies.user.application.port.inbound.UpdateUserPassword
import com.munchies.user.application.usecase.GetUserUseCase
import com.munchies.user.application.usecase.LoginUserUseCase
import com.munchies.user.application.usecase.RegisterUserUseCase
import com.munchies.user.application.usecase.UpdateUserPasswordUseCase
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
}
