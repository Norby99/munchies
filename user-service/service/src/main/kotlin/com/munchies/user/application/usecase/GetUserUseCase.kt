package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.GetUserQuery
import com.munchies.user.application.port.inbound.GetUserQuery.Companion.GetUserResult
import com.munchies.user.application.port.inbound.GetUserQuery.Companion.GetUserResult.NotFound
import com.munchies.user.application.port.inbound.GetUserQuery.Companion.GetUserResult.Success
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.port.UserRepository

class GetUserUseCase(private val repository: UserRepository) : GetUserQuery {
  override fun execute(id: UserId): GetUserResult {
    val user = repository.findById(id)
    return user?.let { Success(it) } ?: NotFound
  }
}
