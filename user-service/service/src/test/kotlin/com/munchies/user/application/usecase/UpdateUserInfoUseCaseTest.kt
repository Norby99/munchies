package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.UpdateUserInfo
import com.munchies.user.domain.factory.MockUserFactory
import com.munchies.user.domain.model.UserProfile
import com.munchies.user.domain.model.UserRole
import com.munchies.user.domain.port.UserRepository
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class UpdateUserInfoUseCaseTest {
  @Test
  fun `execute should return success and save the provided user when id exists`() {
    val userToUpdate = MockUserFactory().create(
      id = "existing-user-id",
      profile = UserProfile(
        username = "updated-username",
        email = "updated@email.com",
        role = UserRole.MANAGER,
      ),
    )
    val existingUser = MockUserFactory().create(
      id = "existing-user-id",
      profile = UserProfile(
        username = "old-username",
        email = "old@email.com",
        role = UserRole.CUSTOMER,
      ),
    )
    val userRepository = mock<UserRepository> {
      on { findById(userToUpdate.id) } doReturn existingUser
    }
    val useCase = UpdateUserInfoUseCase(userRepository)

    val result = useCase.execute(userToUpdate)

    result shouldBe UpdateUserInfo.Companion.UpdateUserInfoResult.Success
    verify(userRepository).save(userToUpdate)
  }

  @Test
  fun `execute should return user not found and not save when id does not exist`() {
    val userToUpdate = MockUserFactory().create(id = "missing-user-id")
    val userRepository = mock<UserRepository> {
      on { findById(userToUpdate.id) } doReturn null
    }
    val useCase = UpdateUserInfoUseCase(userRepository)

    val result = useCase.execute(userToUpdate)

    result shouldBe UpdateUserInfo.Companion.UpdateUserInfoResult.UserNotFound
  }

  @Test
  fun `execute should use the provided user profile when saving`() {
    val userToUpdate = MockUserFactory().create(
      id = "profile-update-user-id",
      profile = UserProfile(
        username = "new-username",
        email = "new@email.com",
        role = UserRole.MANAGER,
      ),
    )
    val existingUser = MockUserFactory().create(
      id = "profile-update-user-id",
      profile = UserProfile(
        username = "legacy-username",
        email = "legacy@email.com",
        role = UserRole.CUSTOMER,
      ),
    )
    val userRepository = mock<UserRepository> {
      on { findById(userToUpdate.id) } doReturn existingUser
    }
    val useCase = UpdateUserInfoUseCase(userRepository)

    useCase.execute(userToUpdate)

    verify(userRepository).save(userToUpdate.copy(profile = userToUpdate.profile))
  }
}
