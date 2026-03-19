package com.munchies.user.infrastructure.service

import com.munchies.user.domain.service.UserService
import com.munchies.user.infrastructure.repository.UserRepositoryImpl
import jakarta.inject.Singleton
object Service {
  @Singleton
  class ControllerService : UserService(UserRepositoryImpl())
}
