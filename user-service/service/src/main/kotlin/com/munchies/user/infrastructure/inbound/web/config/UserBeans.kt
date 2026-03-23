package com.munchies.user.infrastructure.inbound.web.config

import com.munchies.user.application.port.inbound.GetUserQuery
import com.munchies.user.application.usecase.GetUserUseCase
import com.munchies.user.domain.port.UserRepository
import com.munchies.user.infrastructure.outbound.memory.UserRepositoryImpl
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

@Factory
class UserBeans {

  @Singleton
  fun userRepository(): UserRepository = UserRepositoryImpl()

  @Singleton
  fun getUserQuery(repo: UserRepository): GetUserQuery = GetUserUseCase(repo)
}
