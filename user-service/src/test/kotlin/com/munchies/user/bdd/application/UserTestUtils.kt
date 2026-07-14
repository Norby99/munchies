package com.munchies.user.bdd.application

import com.munchies.user.domain.model.*
import com.munchies.user.domain.port.UserCredentialsRepository
import com.munchies.user.domain.port.UserRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class UserContext {
  lateinit var id: UserId
  lateinit var profile: UserProfile
  lateinit var email: String
  lateinit var credentials: UserCredentials
}

@Singleton
class UserHelper @Inject constructor (
  private val userRepository: UserRepository,
  private val credentialsRepository: UserCredentialsRepository
){
  fun createUser(context: UserContext): User {
    val user = User.factory.create(context.id.value, context.profile).shouldBeSuccess()
    userRepository.save(user)
    credentialsRepository.save(context.credentials)
    return user
  }


}
