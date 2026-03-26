package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.CreateNewUser
import com.munchies.user.application.port.inbound.CreateNewUser.Companion.CreateNewUserResult
import com.munchies.user.domain.port.UserRepository

class CreateNewUserUseCase(private val repository: UserRepository) : CreateNewUser {
  override fun execute(): CreateNewUserResult = CreateNewUserResult.Success(repository.create())
}
