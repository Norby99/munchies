package com.munchies.user.adapter.inbound.web.controller

import com.munchies.user.application.port.inbound.GetUser
import com.munchies.user.application.port.inbound.GetUser.Companion.GetUserResult.Success
import com.munchies.user.application.port.inbound.LoginUser
import com.munchies.user.application.port.inbound.RegisterUser
import com.munchies.user.domain.factory.MockUserFactory
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.model.UserId
import com.munchies.user.infrastructure.adapter.dto.factory.UserDTOFactory
import com.munchies.user.infrastructure.adapter.inbound.web.controller.MicronautUserController
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.serde.annotation.SerdeImport
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findAnnotations
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class UserControllerTest {

  private fun getController(
    getUser: GetUser = mock(),
    registerUser: RegisterUser = mock(),
    loginUser: LoginUser = mock(),
    dtoFactory: UserDTOFactory = UserDTOFactory.default,
  ) = MicronautUserController(getUser, registerUser, loginUser, dtoFactory)

  private val dtoFactory = UserDTOFactory.default

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
    val controller = getController(getUser = userUseCase)

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

    val controller = getController(getUser = userUseCase)

    val res = controller.getUser(userId.value)
    res.status shouldBe HttpStatus.OK
    res.body() shouldNotBe null
    res.body().id shouldBe userId.value
    verify(userUseCase).execute(userId)
  }

  @Test
  fun `controller should return ok when registerUser succeeds`() {
    val user = MockUserFactory().create("new-user-id")
    val userDTO = dtoFactory.run { user.fromDomain() }
    val registerUseCase = mock<RegisterUser> {
      on { execute(any(), any()) } doReturn RegisterUser.Companion.RegisterUserResult.Success(user)
    }
    val controller = getController(registerUser = registerUseCase)

    val response = controller.registerUser(
      userInfo = userDTO,
      hashedPassword = "hashed-password",
      saltValue = "salt-value",
    )

    response.status shouldBe HttpStatus.OK
    response.body() shouldBe "User registered successfully"

    val userCaptor = argumentCaptor<User>()
    val credentialsCaptor = argumentCaptor<UserCredentials>()
    verify(registerUseCase).execute(userCaptor.capture(), credentialsCaptor.capture())
    userCaptor.firstValue shouldBe dtoFactory.run { userDTO.fromDTO() }
    credentialsCaptor.firstValue shouldBe UserCredentials(
      id = user.id,
      passwordHash = "hashed-password",
      salt = "salt-value",
    )
  }

  @Test
  fun `controller should return bad request when registerUser reports user already registered`() {
    val userDTO = dtoFactory.run { MockUserFactory().create("existing-user-id").fromDomain() }
    val registerUseCase = mock<RegisterUser> {
      on {
        execute(any(), any())
      } doReturn RegisterUser.Companion.RegisterUserResult.UserIsAlreadyRegistered
    }
    val controller = getController(registerUser = registerUseCase)

    val response = controller.registerUser(
      userInfo = userDTO,
      hashedPassword = "hashed-password",
      saltValue = "salt-value",
    )

    response.status shouldBe HttpStatus.BAD_REQUEST
    response.body() shouldBe "User is already registered"
  }

  @Test
  fun `controller should return server error when registerUser fails`() {
    val userDTO = dtoFactory.run { MockUserFactory().create("failing-user-id").fromDomain() }
    val registerUseCase = mock<RegisterUser> {
      on {
        execute(any(), any())
      } doReturn RegisterUser.Companion.RegisterUserResult.Failure("persistence unavailable")
    }
    val controller = getController(registerUser = registerUseCase)

    val response = controller.registerUser(
      userInfo = userDTO,
      hashedPassword = "hashed-password",
      saltValue = "salt-value",
    )

    response.status shouldBe HttpStatus.INTERNAL_SERVER_ERROR
    response.body() shouldBe "Failed to register user: Failure(reason=persistence unavailable)"
  }

  @Test
  fun `controller should return ok when loginUser succeeds`() {
    val userDTO = dtoFactory.run { MockUserFactory().create("login-success-id").fromDomain() }
    val loginUseCase = mock<LoginUser> {
      on { execute(userDTO.email, userDTO.username, "valid-password") } doReturn
        LoginUser.Companion.LoginResult.Success("login-success-id")
    }
    val controller = getController(loginUser = loginUseCase)

    val response = controller.loginUser(userDTO, "valid-password")

    response.status shouldBe HttpStatus.OK
    verify(loginUseCase).execute(userDTO.email, userDTO.username, "valid-password")
  }

  @Test
  fun `controller should return bad request when loginUser fails`() {
    val userDTO = dtoFactory.run { MockUserFactory().create("login-failure-id").fromDomain() }
    val loginUseCase = mock<LoginUser> {
      on { execute(userDTO.email, userDTO.username, "invalid-password") } doReturn
        LoginUser.Companion.LoginResult.Failure
    }
    val controller = getController(loginUser = loginUseCase)

    val response = controller.loginUser(userDTO, "invalid-password")

    response.status shouldBe HttpStatus.BAD_REQUEST
    verify(loginUseCase).execute(userDTO.email, userDTO.username, "invalid-password")
  }

  @Test
  fun `controller should return bad request when loginUser is blocked`() {
    val userDTO = dtoFactory.run { MockUserFactory().create("login-blocked-id").fromDomain() }
    val loginUseCase = mock<LoginUser> {
      on { execute(userDTO.email, userDTO.username, "blocked-password") } doReturn
        LoginUser.Companion.LoginResult.BlockedLogin
    }
    val controller = getController(loginUser = loginUseCase)

    val response = controller.loginUser(userDTO, "blocked-password")

    response.status shouldBe HttpStatus.UNAUTHORIZED
    verify(loginUseCase).execute(userDTO.email, userDTO.username, "blocked-password")
  }
}
