package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.UpdateUserInfo
import com.munchies.user.domain.model.*
import com.munchies.user.domain.port.UserRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class UpdateUserInfoUseCaseTest {
  @Test
  fun `execute should return success and save the provided user when id exists`() {
    val userToUpdate = exampleUser
    val userRepository = mock<UserRepository> {
      on { findById(userToUpdate.id) } doReturn userToUpdate
    }
    val useCase = UpdateUserInfoUseCase(userRepository)

    val result = useCase.execute(userToUpdate)

    result shouldBe UpdateUserInfo.Companion.UpdateUserInfoResult.Success
    verify(userRepository).save(userToUpdate)
  }

  @Test
  fun `execute should return user not found and not save when id does not exist`() {
    val userToUpdate = exampleUser
    val userRepository = mock<UserRepository> {
      on { findById(userToUpdate.id) } doReturn null
    }
    val useCase = UpdateUserInfoUseCase(userRepository)

    val result = useCase.execute(userToUpdate)

    result shouldBe UpdateUserInfo.Companion.UpdateUserInfoResult.UserNotFound
  }

  @Test
  fun `execute should use the provided user profile when saving`() {
    val userToUpdate = exampleUser

    val newProfile = UserProfile(
      username = "legacy-username",
      email = Email("legacy@email.com"),
      role = UserRole.CUSTOMER,
    )

    val newUser = userToUpdate.update(
      id = userToUpdate.id,
      profile = newProfile,
    )

    val userRepository = mock<UserRepository> {
      on { findById(userToUpdate.id) } doReturn userToUpdate
      on { save(newUser) } doAnswer {}
    }
    val useCase = UpdateUserInfoUseCase(userRepository)

    useCase.execute(newUser)
      .shouldBeInstanceOf<UpdateUserInfo.Companion.UpdateUserInfoResult.Success>()

    verify(userRepository).save(newUser)
    verify(userRepository).findById(userToUpdate.id)
  }
}
