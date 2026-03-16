package com.munchies.user.controller

import com.munchies.commons.InMemoryRepository
import com.munchies.user.api.UserApi
import com.munchies.user.domain.User
import com.munchies.user.domain.UserId
import jakarta.inject.Singleton

@Singleton
class UserService(
  private val repository: InMemoryRepository<UserId, User>,
) : UserApi<UserId, User> {
  override fun getUser(id: UserId): User =
    repository.findById(id) ?: throw NoSuchElementException("User with id $id not found")
}
