package com.munchies.e2e.steps

import com.munchies.e2e.fixtures.user.UserCredentials
import com.munchies.e2e.support.ServiceUrls
import com.munchies.e2e.support.WordResult
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient

class RegisterUserSteps(private val world: WordResult) {

  private val client = HttpClient.create(java.net.URI(ServiceUrls.gateway).toURL())
  private var requestBody: String? = null

  val request = UserCredentials.getRegisterUserRequest()

  @Given("a user which doesnt exist yet")
  fun aUserWhichDoesntExist() {
    requestBody = request.toJson()
  }

  @When("the user registers its info")
  fun theUserRegistersItsInfo() {
    val response = client.toBlocking().exchange(
      HttpRequest.POST("/users/register", requestBody).contentType
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

  @Then("the user receives a personal auth token")
  fun theUserReceivesAPersonalAuthToken() {
    world.authCookie.shouldNotBeNull()
  }

  @And("the user can query its info")
  fun theUserCanQueryItsInfo() {
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
  }
}
