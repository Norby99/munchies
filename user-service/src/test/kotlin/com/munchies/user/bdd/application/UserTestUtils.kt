package com.munchies.user.bdd.application

import com.munchies.user.domain.model.*
import com.munchies.user.domain.port.UserCredentialsRepository
import com.munchies.user.domain.port.UserRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class UserContext() {
  lateinit var id: UserId
  lateinit var profile: UserProfile
  lateinit var email: String
  lateinit var credentials: UserCredentials
  lateinit var result: Any
}

@Singleton
class UserHelper @Inject constructor(
  val userRepository: UserRepository,
  val credentialsRepository: UserCredentialsRepository,
) {
  fun createUser(context: UserContext): User {
    val user = User.factory.create(context.id.value, context.profile).shouldBeSuccess()
    return user
  }

  fun saveUser(user: User) = userRepository.save(user)
  fun saveCredentials(credentials: UserCredentials) = credentialsRepository.save(credentials)

  val exampleId = UserId()

  val exampleUser = User.factory.create(
    exampleId.value,
    profile = UserProfile("John Doe", Email("john.doe@example.com"), UserRole.CUSTOMER),
  ).shouldBeSuccess()
  val exampleCredendials = UserCredentials(exampleId, "password", "salt")
}
