package com.munchies.e2e.steps

import com.munchies.commons.domain.port.AuthRole
import com.munchies.e2e.support.ServiceUrls
import com.munchies.e2e.support.WordResult
import com.munchies.order.infrastructure.adapter.dto.OrderItemDto
import com.munchies.order.infrastructure.adapter.dto.OrderType
import com.munchies.order.infrastructure.adapter.inbound.request.PlaceOrderRequest
import com.munchies.user.infrastructure.adapter.inbound.request.RegisterUserRequest
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import java.util.*

class PlaceOrderSteps(private val world: WordResult) {

  private val client = HttpClient.create(java.net.URI(ServiceUrls.gateway).toURL())

  private var requestBody: String? = null
  private var loginBody: String? = null

  fun loginUser() {
    loginBody =
      RegisterUserRequest(
        username = "pippo" + Random().nextFloat(),
        email = "nigger@nigger.com" + Random().nextFloat(),
        role = AuthRole.CUSTOMER.toString(),
        hashedPassword = "123",
        saltValue = "c",
      ).toJson().trim()

    println(loginBody)
  }

  @Given("a valid delivery order")
  fun aValidDeliveryOrder() {
    requestBody = PlaceOrderRequest(
      restaurantId = "rest-munchies-99",
      customerId = "cust-jack-01",
      items = listOf(
        OrderItemDto(menuItemId = "burger-super-double", quantity = 1),
      ),
      orderType = OrderType.DELIVERY,
      estimatedDeliveryTime = "1814380800000",
      deliveryAddress = "Via Strombi 67",
      bellName = "Antonio",
      customerPhone = "+39333998877",
    ).toJson()
  }

  @When("the client places the order")
  fun placeTheOrder() {
    loginUser()
    try {
      val response = client.toBlocking().exchange(
        HttpRequest.POST("/users/register", loginBody)
          .contentType("application/json"),
        String::class.java,
      )
      world.responseStatus = response.status.code
      world.responseBody = response.body()
      world.cookies = response.cookies
    } catch (e: HttpClientResponseException) {
      world.responseStatus = e.status.code
      world.responseBody = e.response.getBody(String::class.java).orElse(null)
    }

    println("STATUS: ${world.responseStatus}")
    println("BODY: ${world.responseBody}")

    try {
      val response = client.toBlocking().exchange(
        HttpRequest.POST("/orders/place", requestBody).cookie(world.cookies!!.get("authToken"))
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

  @Then("order is created successfully")
  fun orderIsCreatedSuccessfully() {
    println("STATUS: ${world.responseStatus}")
    println("BODY: ${world.responseBody}")

    world.responseStatus shouldBe 200
  }
}
