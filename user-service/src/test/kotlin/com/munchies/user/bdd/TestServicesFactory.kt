package com.munchies.user.bdd

import com.munchies.commons.repository.InMemoryRepository
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.port.PasswordHasher
import com.munchies.user.domain.port.UserCredentialsRepository
import com.munchies.user.domain.port.UserRepository
import com.munchies.user.infrastructure.adapter.outbound.hash.KotlinPasswordHasher
import com.munchies.user.infrastructure.adapter.outbound.memory.MemoryUserCredentialsRepository
import com.munchies.user.infrastructure.adapter.outbound.memory.MemoryUserRepository
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

@Factory
class TestServicesFactory {


  @Singleton
  fun memoryUserRepository(): UserRepository =
    object : MemoryUserRepository {
      override val repository: InMemoryRepository<UserId, User> =
        InMemoryRepository()
    }

  @Singleton
  fun memoryUserCredentialsRepository(): UserCredentialsRepository =
    object : MemoryUserCredentialsRepository {
      override val repository: InMemoryRepository<UserId, UserCredentials> =
        InMemoryRepository()
    }

  @Singleton
  fun hasher(): PasswordHasher = KotlinPasswordHasher()


}
