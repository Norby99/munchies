package com.munchies.e2e.steps

import com.munchies.e2e.support.ServiceUrls
import com.munchies.e2e.support.WordResult
import com.munchies.order.infrastructure.adapter.inbound.request.AdvanceOrderStatusRequest
import com.munchies.order.infrastructure.adapter.outbound.response.placeOrderResponseFromJson
import io.cucumber.java.en.And
import io.cucumber.java.en.Then
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException

class AdvanceOrderStatusSteps(private val world: WordResult) {

  private val client = HttpClient.create(java.net.URI(ServiceUrls.gateway).toURL())

  @And("the client advances the order status")
  fun theClientAdvancesTheOrderStatus() {
    val placedOrder = placeOrderResponseFromJson(world.responseBody!!)
    world.orderId = placedOrder.result.orderId

    try {
      val response = client.toBlocking().exchange(
        HttpRequest.POST(
          "/orders/advance",
          AdvanceOrderStatusRequest(world.orderId!!).toJson(),
        )
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

  @Then("order status is advanced successfully")
  fun orderStatusIsAdvancedSuccessfully() {
    println("STATUS: ${world.responseStatus}")
    println("BODY: ${world.responseBody}")
    world.responseStatus shouldBe 200
  }

  /**
   * Kafka delivery to notification-service is asynchronous (see the event-driven
   * skill), and the topic can carry a backlog from earlier local/CI runs (Kafka
   * volumes are only wiped with `-Preset`, see task.gradle.kts), so a single
   * fixed sleep + `--tail` isn't reliable: this polls the container's console
   * output until it contains this specific order's id, which is how the (still
   * stub) handler logs the received OrderStatusChangedNotification.
   */
  @Then("notification-service should have logged the order status change")
  fun notificationServiceShouldHaveLoggedTheOrderStatusChange() {
    val deadline = System.currentTimeMillis() + 10_000
    var logs: String
    do {
      logs = readNotificationServiceLogs()
      if (logs.contains(world.orderId!!)) break
      Thread.sleep(500)
    } while (System.currentTimeMillis() < deadline)

    println("---- notification-service logs (tail) ----")
    println(logs)
    println("-------------------------------------------")

    logs shouldContain world.orderId!!
  }

  private fun readNotificationServiceLogs(): String = try {
    ProcessBuilder("docker", "logs", "--tail", "200", "munchies-notification-service")
      .redirectErrorStream(true)
      .start()
      .inputStream
      .bufferedReader()
      .readText()
  } catch (e: Exception) {
    "Could not read notification-service logs: ${e.message}"
  }
}
