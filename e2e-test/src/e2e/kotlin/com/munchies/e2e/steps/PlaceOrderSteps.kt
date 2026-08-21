package com.munchies.e2e.steps

import com.munchies.e2e.support.ServiceUrls
import com.munchies.e2e.support.WordResult
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException

class PlaceOrderSteps(private val world: WordResult) {

  private val client = HttpClient.create(java.net.URI(ServiceUrls.order).toURL())

  private var requestBody: String? = null

  @Given("a valid delivery order")
  fun aValidDeliveryOrder() {
    requestBody = """
      {
        "restaurantId": "rest-munchies-99",
        "customerId": "cust-jack-01",
        "items": [
          { "menuItemId": "burger-super-double", "quantity": 1 }
        ],
        "orderType": "DELIVERY",
        "estimatedDeliveryTime": 1814380800000,
        "deliveryAddress": "Via Stronzi 67",
        "bellName": "Sviluppatore",
        "customerPhone": "+39333998877"
      }
    """.trimIndent()
  }

  @When("the client places the order")
  fun placeTheOrder() {
    try {
      val response = client.toBlocking().exchange(
        HttpRequest.POST("/orders/place", requestBody)
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

  @Then("print the result")
  fun printResult() {
    println("STATUS: ${world.responseStatus}")
    println("BODY: ${world.responseBody}")
  }
}
