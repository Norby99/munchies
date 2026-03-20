package com.munchies.user.domain.service

import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.port.UserRepository
import com.munchies.user.infrastructure.api.UserApi

open class UserService(private val userRepository: UserRepository) : UserApi<UserId, User?> {
  override fun getUser(id: UserId): User? = userRepository.findById(id)
}
