package com.munchies.user.bdd

import com.munchies.user.domain.port.PasswordHasher
import com.munchies.user.domain.port.UserCredentialsRepository
import com.munchies.user.domain.port.UserRepository
import com.munchies.user.infrastructure.adapter.outbound.hash.KotlinPasswordHasher
import com.munchies.user.infrastructure.adapter.outbound.memory.MemoryUserCredentialsRepositoryImpl
import com.munchies.user.infrastructure.adapter.outbound.memory.MemoryUserRepositoryImpl
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

@Factory
class TestServicesFactory {

  @Singleton
  fun memoryUserRepository(): UserRepository = MemoryUserRepositoryImpl()

  @Singleton
  fun memoryUserCredentialsRepository(): UserCredentialsRepository =
    MemoryUserCredentialsRepositoryImpl()

  @Singleton
  fun hasher(): PasswordHasher = KotlinPasswordHasher()
}
