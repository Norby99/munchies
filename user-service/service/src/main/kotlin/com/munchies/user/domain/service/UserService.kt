package com.munchies.user.domain.service

import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.port.UserRepository

open class UserService(private val userRepository: UserRepository) {
  fun getUser(id: UserId) = userRepository.findById(id)
}
