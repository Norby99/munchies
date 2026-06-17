package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.GetUser.Companion.GetUserResult
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.model.exampleUser
import com.munchies.user.domain.model.exampleUserId
import com.munchies.user.domain.port.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class GetUserUseCaseTest {

  @Test
  fun `execute should return NotFound when user is not found`() {
    val userId = UserId("nonexistent-id")
    val repository = mock<UserRepository> {
      on { findById(any()) } doReturn null
    }
    val useCase = GetUserUseCase(repository)
    val result = useCase.execute(userId)

    assertTrue(result is GetUserResult.NotFound)
    verify(repository).findById(userId)
  }

  @Test
  fun `execute should return Success with user when user is found`() {
    val repository = mock<UserRepository> {
      on { findById(any()) } doReturn exampleUser
    }
    val useCase = GetUserUseCase(repository)

    val result = useCase.execute(exampleUserId)
    assertTrue(result is GetUserResult.Success)
    assertEquals(exampleUserId, (result as GetUserResult.Success).user.id)
    verify(repository).findById(exampleUserId)
  }
}
