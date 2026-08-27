package com.munchies.e2e.steps

import com.munchies.e2e.fixtures.user.UserCredentials
import com.munchies.e2e.support.ServiceUrls
import com.munchies.e2e.support.WordResult
import io.cucumber.java.en.Given
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient

class AuthSteps(private val world: WordResult) {

  private val client = HttpClient.create(java.net.URI(ServiceUrls.gateway).toURL())

  @Given("an authenticated client")
  fun anAuthenticatedCustomer() {
    register(UserCredentials.newCustomer())
  }

  @Given("an authenticated manager")
  fun anAuthenticatedManager() {
    register(UserCredentials.newManager())
  }

  fun register(userJson: String) {
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
          })
  }
}
