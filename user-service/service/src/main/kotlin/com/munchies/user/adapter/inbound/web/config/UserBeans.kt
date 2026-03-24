package com.munchies.user.adapter.inbound.web.config

import com.munchies.user.adapter.outbound.memory.UserRepositoryImpl
import com.munchies.user.application.port.inbound.GetUserQuery
import com.munchies.user.application.usecase.GetUserUseCase
import com.munchies.user.domain.port.UserRepository
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

@Factory
class UserBeans {

  @Singleton
  fun userRepository(): UserRepository = UserRepositoryImpl()

  @Singleton
  fun getUserQuery(repo: UserRepository): GetUserQuery = GetUserUseCase(repo)
}
