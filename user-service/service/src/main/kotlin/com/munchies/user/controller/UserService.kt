package com.munchies.user.controller

import com.munchies.user.api.UserApi
import com.munchies.user.domain.User
import com.munchies.user.domain.UserId
import com.munchies.user.repository.UserRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class UserService(
  @Inject
  private val repository: UserRepository,
) : UserApi<UserId, User> {
  override fun getUser(id: UserId): User = repository.findById(id) ?: User(id)
}
