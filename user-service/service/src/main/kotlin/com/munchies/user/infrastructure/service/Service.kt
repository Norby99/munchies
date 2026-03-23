package com.munchies.user.infrastructure.service

import com.munchies.user.domain.port.UserRepository
import com.munchies.user.domain.service.UserService
import com.munchies.user.infrastructure.repository.UserRepositoryImpl
import jakarta.inject.Singleton
object Service {
  @Singleton
  open class ControllerService(
    repository: UserRepository = UserRepositoryImpl(),
  ) : UserService(repository)
}
