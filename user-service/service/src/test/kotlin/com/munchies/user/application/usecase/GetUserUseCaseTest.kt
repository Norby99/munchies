package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.GetUserQuery.Companion.GetUserResult
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.port.UserRepository
import com.munchies.user.presentation.dto.UserDTO
import com.munchies.user.presentation.toDomain
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
    val userId = UserId("real-id")
    val repository = mock<UserRepository> {
      on { findById(any()) } doReturn UserDTO(id = userId.value).toDomain()
    }
    val useCase = GetUserUseCase(repository)

    val result = useCase.execute(userId)
    assertTrue(result is GetUserResult.Success)
    assertEquals(userId, (result as GetUserResult.Success).user.id)
    verify(repository).findById(userId)
  }
}
