package com.munchies.user.application.usecase

import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.port.UserRepository

class GetUserUseCase(private val userRepository: UserRepository) {
  fun execute(id: UserId) = userRepository.findById(id)
}
