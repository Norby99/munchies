package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.CreateNewUser.Companion.CreateNewUserResult
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.port.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class CreateNewUserCaseTest {
  @Test
  fun `execute should create a new user and return its id`() {
    val userId = UserId("new-user-id")
    val repository: UserRepository = mock<UserRepository> {
      on { create() } doReturn userId
    }

    val useCase = CreateNewUserUseCase(repository)
    val result = useCase.execute()

    verify(repository).create()
    assert((result as CreateNewUserResult.Success).userId == userId)
  }
}
