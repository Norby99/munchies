package com.munchies.user.adapter.inbound.web.controller

import com.munchies.user.application.port.inbound.CreateNewUser
import com.munchies.user.application.port.inbound.CreateNewUser.Companion.CreateNewUserResult
import com.munchies.user.application.port.inbound.GetUserQuery
import com.munchies.user.domain.model.UserId
import com.munchies.user.presentation.dto.UserDTO
import com.munchies.user.presentation.toDomain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.serde.annotation.SerdeImport
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findAnnotations
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class UserControllerTest {

  private fun getController(
    getUserQuery: GetUserQuery = mock(),
    createNewUser: CreateNewUser = mock(),
  ) = UserController(getUserQuery, createNewUser)

  @Test
  fun `controller is @Controller annotated`() {
    val controller = UserController::class
    controller.findAnnotation<Controller>() shouldNotBe null
  }

  @Test
  fun `controller should have @SerdeImport for DTOs`() {
    val controller = UserController::class
    val serdeImport = controller.findAnnotations<SerdeImport>()
    serdeImport shouldNotBe null
    serdeImport.shouldNotBeEmpty()
  }

  @Test
  fun `controller should respond NotFound for getUser not found`() {
    val userId = UserId("nonexistent")
    val userUseCase = mock<GetUserQuery> {
      on { execute(userId) } doReturn GetUserQuery.Companion.GetUserResult.NotFound
    }
    val controller = getController(getUserQuery = userUseCase)

    val res = controller.getUser(userId.value)
    res.status shouldBe HttpStatus.NOT_FOUND
    verify(userUseCase).execute(userId)
  }

  @Test
  fun `controller should return a correct user when getUser`() {
    val userId = UserId("very-real-id")
    val userDTO = UserDTO(id = userId.value)
    val userUseCase = mock<GetUserQuery> {
      on {
        execute(
          userId,
        )
      } doReturn GetUserQuery.Companion.GetUserResult.Success(userDTO.toDomain())
    }

    val controller = getController(getUserQuery = userUseCase)

    val res = controller.getUser(userId.value)
    res.status shouldBe HttpStatus.OK
    res.body() shouldNotBe null
    res.body().id shouldBe userId.value
    verify(userUseCase).execute(userId)
  }

  @Test
  fun `controller should create a new user and return its id`() {
    val newUserId = UserId("new-user-id")
    val createNewUser = mock<CreateNewUser> {
      on { execute() } doReturn CreateNewUserResult.Success(newUserId)
    }
    val controller = getController(createNewUser = createNewUser)

    val res = controller.addUser()
    res.status shouldBe HttpStatus.CREATED
    res.body() shouldBe newUserId.value
    verify(createNewUser).execute()
  }

  @Test
  fun `controller should create a new user and be able to get it`() {
    val newUserId = UserId("new-user-id")
    val userDTO = UserDTO(id = newUserId.value)
    val createNewUser = mock<CreateNewUser> {
      on { execute() } doReturn CreateNewUserResult.Success(newUserId)
    }

    val getUserQuery = mock<GetUserQuery> {
      on {
        execute(
          newUserId,
        )
      } doReturn GetUserQuery.Companion.GetUserResult.Success(userDTO.toDomain())
    }

    val controller = getController(createNewUser = createNewUser, getUserQuery = getUserQuery)

    val createRes = controller.addUser()
    createRes.status shouldBe HttpStatus.CREATED
    createRes.body() shouldBe newUserId.value

    val getRes = controller.getUser(newUserId.value)
    getRes.status shouldBe HttpStatus.OK
    getRes.body() shouldNotBe null
    getRes.body().id shouldBe newUserId.value
  }
}
