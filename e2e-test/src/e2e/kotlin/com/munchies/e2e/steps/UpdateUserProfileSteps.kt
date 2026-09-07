package com.munchies.e2e.steps

import com.munchies.e2e.fixtures.user.UserCredentials
import com.munchies.e2e.support.ServiceUrls
import com.munchies.e2e.support.WordResult
import com.munchies.user.infrastructure.adapter.dto.UserDTO
import com.munchies.user.infrastructure.adapter.inbound.request.UpdateUserInfoRequest
import com.munchies.user.infrastructure.adapter.outbound.response.getUserResponseFromJson
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException

class UpdateUserProfileSteps(private val world: WordResult) {
  private val client = HttpClient.create(java.net.URI(ServiceUrls.gateway).toURL())

  val request = UserCredentials.getRegisterUserRequest()

  @Given("a user which is registered")
  fun aUserWhichIsRegistered() {
    register(request.toJson())
  }

  val updateRequest = UpdateUserInfoRequest(
    UserDTO(
      id = "",
      username = "",
      email = request.email,
      role = request.role,
    ),
  )

  @When("the user updates its profile")
  fun theUserUpdatesItsProfile() {
    try {
      val response = client.toBlocking().exchange(
        HttpRequest.PATCH("/users/update-info/", updateRequest.toJson())
          .cookie(world.authCookie)
          .contentType("application/json"),
        String::class.java,
      )
      world.responseStatus = response.status.code
      world.responseBody = response.body()
    } catch (e: HttpClientResponseException) {
      world.responseStatus = e.status.code
      world.responseBody = e.response.getBody(String::class.java).orElse(null)
    }
  }

  @Then("only the correct changes are allowed")
  fun onlyTheCorrectChangesAreAllowed() {
    world.responseStatus.shouldBe(400)
    val updateRequest = UpdateUserInfoRequest(
      UserDTO(
        id = "",
        username = request.username + " new",
        email = request.email,
        role = request.role,
      ),
    )

    try {
      val response = client.toBlocking().exchange(
        HttpRequest.PATCH("/users/update-info/", updateRequest.toJson())
          .cookie(world.authCookie)
          .contentType("application/json"),
        String::class.java,
      )
      world.responseStatus = response.status.code
      world.responseBody = response.body()
    } catch (e: HttpClientResponseException) {
      world.responseStatus = e.status.code
      world.responseBody = e.response.getBody(String::class.java).orElse(null)
    }

    world.responseStatus.shouldBe(200)
  }

  @And("the user has its profile updated")
  fun theUserHasItsProfileUpdated() {
    val response = client.toBlocking()
      .exchange(
        HttpRequest.GET<String>("/users/")
          .cookie(world.authCookie)
          .contentType("application/json"),
        String::class.java,
      )
    world.responseStatus = response.status.code
    world.responseBody = response.body()

    world.responseStatus shouldBe 200
    world.responseBody.shouldNotBeNull()
    getUserResponseFromJson(world.responseBody!!)
      .result.username.shouldBe(request.username + " new")
  }

  private fun register(userJson: String) {
    val response = client.toBlocking().exchange(
      HttpRequest.POST("/users/register", userJson).contentType
        ("application/json"),
      String::class.java,
    )

    world.authCookie = response.cookies["authToken"]
      ?: error(
        "User registered but no auth cookie was given: " +
          response.cookies.forEach { c ->
            println("${c.key} = ${c.value}")
          },
      )
  }
}
