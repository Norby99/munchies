package com.munchies.user.infrastructure.adapter.inbound.web.controller

import com.munchies.user.application.port.inbound.*
import com.munchies.user.application.port.inbound.GetUser.Companion.GetUserResult.Success
import com.munchies.user.application.usecase.VerifyUserEmailUseCase
import com.munchies.user.domain.factory.MockUserFactory
import com.munchies.user.domain.model.Email
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.model.UserProfile
import com.munchies.user.domain.port.PasswordHasher
import com.munchies.user.domain.port.TokenProvider
import com.munchies.user.domain.port.TokenProvider.Companion.GenerateTokenResult
import com.munchies.user.domain.port.UserRepository
import com.munchies.user.infrastructure.adapter.dto.factory.UserDTOFactory
import com.munchies.user.infrastructure.adapter.inbound.request.LoginUserRequest
import com.munchies.user.infrastructure.adapter.inbound.request.RegisterUserRequest
import com.munchies.user.infrastructure.adapter.inbound.request.UpdateUserInfoRequest
import com.munchies.user.infrastructure.adapter.inbound.request.UpdateUserPasswordRequest
import com.munchies.user.infrastructure.adapter.inbound.web.config.UserServices
import com.munchies.user.infrastructure.adapter.outbound.kafka.EmailConfirmationClient
import com.munchies.user.infrastructure.adapter.outbound.memory.MemoryUserRepositoryTest
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.serde.annotation.SerdeImport
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findAnnotations
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class UserControllerTest {

  private val fakeServices = UserServices(
    getUser = mock(),
    registerUser = mock(),
    loginUser = mock(),
    updateUserPassword = mock(),
    updateUserInfo = mock(),
    deleteUser = mock(),
    verifyUserEmail = mock(),
  )

  private fun getController(
    services: UserServices = fakeServices,
    dtoFactory: UserDTOFactory = UserDTOFactory.default,
    emailConfirmationKafkaClient: EmailConfirmationClient = mock(),
    tokenProvider: TokenProvider = mock(),
  ) = MicronautUserController(
    services = services,
    dtoFactory = dtoFactory,
    emailConfirmationKafkaClient = emailConfirmationKafkaClient,
    paymentClient = mock(),
    tokenProvider = tokenProvider,
  )

  private val dtoFactory = UserDTOFactory.default

  private val validUserDto = dtoFactory.run {
    MockUserFactory()
      .create(
        "valid-user-id",
        profile = UserProfile.empty
          .copy(username = "valid-username", email = Email("valid-email")),
      )
      .fromDomain()
  }

  @Test
  fun `controller is @Controller annotated`() {
    val controller = MicronautUserController::class
    controller.findAnnotation<Controller>() shouldNotBe null
  }

  @Test
  fun `controller should have @SerdeImport for DTOs`() {
    val controller = MicronautUserController::class
    val serdeImport = controller.findAnnotations<SerdeImport>()
    serdeImport shouldNotBe null
    serdeImport.shouldNotBeEmpty()
  }

  @Test
  fun `controller should respond NotFound for getUser not found`() {
    val userId = UserId("nonexistent")
    val userUseCase = mock<GetUser> {
      on { execute(userId) } doReturn GetUser.Companion.GetUserResult.NotFound
    }
    val controller = getController(fakeServices.copy(getUser = userUseCase))

    val res = controller.getUser(userId.value)
    res.status shouldBe HttpStatus.NOT_FOUND
    verify(userUseCase).execute(userId)
  }

  @Test
  fun `controller should return a correct user when getUser`() {
    val userId = UserId("very-real-id")
    val userDTO = dtoFactory.run { MockUserFactory().create(userId.value).fromDomain() }
    val userUseCase = mock<GetUser> {
      on {
        execute(
          userId,
        )
      } doReturn Success(dtoFactory.run { userDTO.fromDTO() })
    }

    val controller = getController(fakeServices.copy(getUser = userUseCase))

    val res = controller.getUser(userId.value)
    res.status shouldBe HttpStatus.OK
    res.body() shouldNotBe null
    res.body().id shouldBe userId.value
    verify(userUseCase).execute(userId)
  }

  @Test
  fun `controller should return ok when registerUser succeeds`() {
    val user = MockUserFactory().create("new-user-id")
    val userDTO = validUserDto
    val registerUseCase = mock<RegisterUser> {
      on { execute(any(), any()) } doReturn RegisterUser.Companion.RegisterUserResult.Success(user)
    }
    val controller = getController(
      fakeServices.copy(registerUser = registerUseCase),
      tokenProvider = mock {
        on { generateToken(any()) } doReturn GenerateTokenResult.Success("sometoken")
      },
    )

    val response = controller.registerUser(
      RegisterUserRequest(
        user = userDTO,
        hashedPassword = "hashed-password",
        saltValue = "salt-value",
      ),
    )

    response.status shouldBe HttpStatus.OK
    response.cookies.all.shouldNotBeEmpty()
    response.body().shouldNotBeEmpty()
  }

  @Test
  fun `controller should return bad request when registerUser reports user already registered`() {
    val userDTO = validUserDto
    val registerUseCase = mock<RegisterUser> {
      on {
        execute(any(), any())
      } doReturn RegisterUser.Companion.RegisterUserResult.UserIsAlreadyRegistered
    }
    val controller = getController(fakeServices.copy(registerUser = registerUseCase))

    val response = controller.registerUser(

      RegisterUserRequest(
        user = userDTO,
        hashedPassword = "hashed-password",
        saltValue = "salt-value",
      ),
    )

    response.status shouldBe HttpStatus.UNAUTHORIZED
  }

  @Test
  fun `controller should return server error when registerUser fails`() {
    val userDTO = validUserDto
    val registerUseCase = mock<RegisterUser> {
      on {
        execute(any(), any())
      } doReturn RegisterUser.Companion.RegisterUserResult.Failure("persistence unavailable")
    }
    val controller = getController(fakeServices.copy(registerUser = registerUseCase))

    val response = controller.registerUser(
      RegisterUserRequest(
        user = userDTO,
        hashedPassword = "hashed-password",
        saltValue = "salt-value",
      ),
    )

    response.status shouldBe HttpStatus.INTERNAL_SERVER_ERROR
    response.body().shouldNotBeEmpty()
  }

  @Test
  fun `controller should return ok when loginUser succeeds`() {
    val userDTO = validUserDto
    val loginUseCase = mock<LoginUser> {
      on { execute(userDTO.email, userDTO.username, "valid-password") } doReturn
        LoginUser.Companion.LoginResult.Success("login-success-id")
    }
    val controller = getController(
      fakeServices.copy(loginUser = loginUseCase),
      tokenProvider = mock {
        on { generateToken(any()) } doReturn GenerateTokenResult.Success("sometoken")
      },
    )

    val response = controller
      .loginUser(LoginUserRequest(userDTO.email, userDTO.username, "valid-password"))

    response.status shouldBe HttpStatus.OK
    response.cookies.all.shouldNotBeEmpty()
    verify(loginUseCase).execute(userDTO.email, userDTO.username, "valid-password")
  }

  @Test
  fun `controller should return bad request when loginUser fails`() {
    val userDTO = validUserDto
    val loginUseCase = mock<LoginUser> {
      on { execute(userDTO.email, userDTO.username, "invalid-password") } doReturn
        LoginUser.Companion.LoginResult.Failure
    }
    val controller = getController(fakeServices.copy(loginUser = loginUseCase))

    val response = controller
      .loginUser(LoginUserRequest(userDTO.email, userDTO.username, "invalid-password"))

    response.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `controller should return bad request when loginUser is blocked`() {
    val userDTO = validUserDto
    val loginUseCase = mock<LoginUser> {
      on { execute(userDTO.email, userDTO.username, "blocked-password") } doReturn
        LoginUser.Companion.LoginResult.BlockedLogin
    }
    val controller = getController(fakeServices.copy(loginUser = loginUseCase))

    val response = controller
      .loginUser(LoginUserRequest(userDTO.email, userDTO.username, "blocked-password"))

    response.status shouldBe HttpStatus.UNAUTHORIZED
    verify(loginUseCase).execute(userDTO.email, userDTO.username, "blocked-password")
  }

  @Test
  fun `controller should return unauthorized when updateUserPassword user is locked`() {
    val userDTO = validUserDto
    val updatePasswordUseCase = mock<UpdateUserPassword> {
      on { execute(any(), any(), any()) } doReturn
        UpdateUserPassword.Companion.UpdateUserPasswordResult.LockedUser
    }
    val controller = getController(fakeServices.copy(updateUserPassword = updatePasswordUseCase))

    val response = controller
      .updateUserPassword(
        UpdateUserPasswordRequest(userDTO, "old-password", "new-password"),
      )

    response.status shouldBe HttpStatus.UNAUTHORIZED
    verify(updatePasswordUseCase)
      .execute(
        dtoFactory
          .run { userDTO.fromDTO() },
        "old-password",
        "new-password",
      )
  }

  @Test
  fun `controller should return not found when updateUserPassword user not found`() {
    val userDTO = validUserDto
    val updatePasswordUseCase = mock<UpdateUserPassword> {
      on { execute(any(), any(), any()) } doReturn
        UpdateUserPassword.Companion.UpdateUserPasswordResult.UserNotFound
    }
    val controller = getController(fakeServices.copy(updateUserPassword = updatePasswordUseCase))

    val response = controller
      .updateUserPassword(
        UpdateUserPasswordRequest(
          userDTO,
          "old-password",
          "new-password",
        ),
      )

    response.status shouldBe HttpStatus.NOT_FOUND
    verify(updatePasswordUseCase)
      .execute(
        dtoFactory
          .run { userDTO.fromDTO() },
        "old-password",
        "new-password",
      )
  }

  @Test
  fun `controller should return ok when updateUserInfo succeeds`() {
    val userDTO = validUserDto
    val updateInfoUseCase = mock<UpdateUserInfo> {
      on { execute(any()) } doReturn UpdateUserInfo.Companion.UpdateUserInfoResult.Success
    }
    val controller = getController(fakeServices.copy(updateUserInfo = updateInfoUseCase))

    val response = controller.updateUserInfo(UpdateUserInfoRequest(userDTO))

    response.status shouldBe HttpStatus.OK
    response.body() shouldBe "User info updated successfully"
    verify(updateInfoUseCase).execute(dtoFactory.run { userDTO.fromDTO() })
  }

  @Test
  fun `controller should return not found when updateUserInfo user not found`() {
    val userDTO = validUserDto
    val updateInfoUseCase = mock<UpdateUserInfo> {
      on { execute(any()) } doReturn UpdateUserInfo.Companion.UpdateUserInfoResult.UserNotFound
    }
    val controller = getController(fakeServices.copy(updateUserInfo = updateInfoUseCase))

    val response = controller.updateUserInfo(UpdateUserInfoRequest(userDTO))

    response.status shouldBe HttpStatus.NOT_FOUND
    verify(updateInfoUseCase).execute(dtoFactory.run { userDTO.fromDTO() })
  }

  @Test
  fun `controller should return bad request when updateUserInfo user id is blank`() {
    val invalidUserDTO = dtoFactory
      .run { MockUserFactory().create("update-info-invalid-id").fromDomain() }
      .copy(id = "")
    val updateInfoUseCase = mock<UpdateUserInfo>()
    val controller = getController(fakeServices.copy(updateUserInfo = updateInfoUseCase))

    val response = controller.updateUserInfo(UpdateUserInfoRequest(invalidUserDTO))

    response.status shouldBe HttpStatus.BAD_REQUEST
    response.body().shouldNotBeEmpty()
  }

  @Test
  fun `controller should return bad request when updateUserInfo username is blank`() {
    val invalidUserDTO = dtoFactory
      .run { MockUserFactory().create("update-info-invalid-username").fromDomain() }
      .copy(username = "")
    val updateInfoUseCase = mock<UpdateUserInfo>()
    val controller = getController(fakeServices.copy(updateUserInfo = updateInfoUseCase))

    val response = controller.updateUserInfo(UpdateUserInfoRequest(invalidUserDTO))

    response.status shouldBe HttpStatus.BAD_REQUEST
    response.body().shouldNotBeEmpty()
  }

  @Test
  fun `controller should return bad request when updateUserInfo email is blank`() {
    val invalidUserDTO = dtoFactory
      .run { MockUserFactory().create("update-info-invalid-email").fromDomain() }
      .copy(email = "")
    val updateInfoUseCase = mock<UpdateUserInfo>()
    val controller = getController(fakeServices.copy(updateUserInfo = updateInfoUseCase))

    val response = controller.updateUserInfo(UpdateUserInfoRequest(invalidUserDTO))

    response.status shouldBe HttpStatus.BAD_REQUEST
    response.body().shouldNotBeEmpty()
  }

  @Test
  fun `controller should return ok when deleteUser succeeds`() {
    val userId = UserId("delete-me-id")
    val userDTO = dtoFactory.run { MockUserFactory().create(userId.value).fromDomain() }
    val deleteUseCase = mock<DeleteUser> {
      on {
        execute(userId)
      } doReturn DeleteUser.Companion.DeleteUserResult.Success(dtoFactory.run { userDTO.fromDTO() })
    }

    val controller = getController(fakeServices.copy(deleteUser = deleteUseCase))

    val response = controller.deleteUser(userId.value)
    response.status shouldBe HttpStatus.OK
    response.body() shouldNotBe null
    response.body().id shouldBe userId.value
    verify(deleteUseCase).execute(userId)
  }

  @Test
  fun `controller should return not found when deleteUser user not found`() {
    val userId = UserId("nonexistent-id")
    val deleteUseCase = mock<DeleteUser> {
      on { execute(userId) } doReturn DeleteUser.Companion.DeleteUserResult.NotFound
    }
    val controller = getController(fakeServices.copy(deleteUser = deleteUseCase))

    val response = controller.deleteUser(userId.value)

    response.status shouldBe HttpStatus.NOT_FOUND
    verify(deleteUseCase).execute(userId)
  }

  @Test
  fun `controller should return in verify-email not found when user doesnt exist`() {
    val userId = "user-id"

    val repo = mock<UserRepository> {
      on { findById(UserId(userId)) } doReturn null
    }
    val hasher = mock<PasswordHasher>()

    val controller = getController(
      fakeServices.copy(
        verifyUserEmail =
        VerifyUserEmailUseCase(repo, hasher),
      ),
    )

    val response = controller.verifyEmail(userId, "otk")

    response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `controller should return in verify-email not found when otk doesnt match`() {
    val userId = "user-id"

    val repo = mock<UserRepository> {
      on { findById(UserId(userId)) } doReturn
        UserDTOFactory.default.run { validUserDto.fromDTO() }
    }
    val hasher = mock<PasswordHasher> {
      on { hash(validUserDto.id, validUserDto.email) } doReturn "otk"
    }

    val controller = getController(
      fakeServices.copy(
        verifyUserEmail =
        VerifyUserEmailUseCase(repo, hasher),
      ),
    )

    val response = controller.verifyEmail(userId, "fakeOtk")

    response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `controller should return ok in verify-email found when user and otk match`() {
    val userId = validUserDto.id

    val repo = MemoryUserRepositoryTest().createMemoryUserRepository()

    repo.save(dtoFactory.run { validUserDto.fromDTO() })

    val hasher = mock<PasswordHasher> {
      on { hash(validUserDto.id, validUserDto.email) } doReturn "otk"
    }

    val notificationClient = mock<EmailConfirmationClient> {
      on { confirmEmail(any()) } doAnswer {}
    }

    val controller = getController(
      fakeServices.copy(
        verifyUserEmail =
        VerifyUserEmailUseCase(repo, hasher),
      ),
      emailConfirmationKafkaClient = notificationClient,
    )

    val response = controller.verifyEmail(userId, "otk")
    verify(notificationClient).confirmEmail(any())
    response.status shouldBe HttpStatus.OK
    repo.findById(UserId(userId)) shouldNotBe null
    repo.findById(UserId(userId))!!.profile.email.isVerified shouldBe true
  }
}
