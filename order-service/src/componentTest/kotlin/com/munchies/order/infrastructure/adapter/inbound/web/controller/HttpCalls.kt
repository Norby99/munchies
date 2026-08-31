package com.munchies.order.infrastructure.adapter.inbound.web.controller

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient
import io.micronaut.http.uri.UriBuilder

/**
 * HttpCalls is a utility class that provides methods for making HTTP requests
 * to the order service.
 *
 * @property baseUrl The base URL of the order service.
 * @property client The HttpClient used to make HTTP requests.
 */
class HttpCalls(val baseUrl: String, val client: HttpClient) {

  /**
   * Makes an HTTP POST request to the specified endpoint with the given request body.
   *
   * @param T The type of the response body expected from the POST request.
   * @param request The request body to be sent in the POST request.
   * @param endPoint The endpoint to which the POST request is made.
   * @return The HttpResponse containing the response from the server.
   */
  inline fun <reified T> httpPost(request: Any, endPoint: String): HttpResponse<T> =
    client.toBlocking()
      .exchange(
        HttpRequest.POST(
          "${baseUrl}$endPoint",
          request,
        ),
        T::class.java,
      )

  /**
   * Makes an HTTP PATCH request to the specified endpoint with the given request body.
   *
   * @param T The type of the response body expected from the PATCH request.
   * @param request The request body to be sent in the PATCH request.
   * @param endPoint The endpoint to which the PATCH request is made.
   * @return The HttpResponse containing the response from the server.
   */
  inline fun <reified T> httpPatch(request: Any, endPoint: String): HttpResponse<T> =
    client.toBlocking().exchange(
      HttpRequest.PATCH(
        "${baseUrl}$endPoint",
        request,
      ),
      T::class.java,
    )

  /**
   * Makes an HTTP GET request to the specified endpoint.
   *
   * @param T The type of the response body expected from the GET request.
   * @param endPoint The endpoint to which the GET request is made.
   * @return The HttpResponse containing the response from the server.
   */
  inline fun <reified T> httpGet(endPoint: String): HttpResponse<T> = client.toBlocking().exchange(
    HttpRequest.GET<T>("${baseUrl}$endPoint"),
    T::class.java,
  )

  /**
   * Makes an HTTP GET request to the get-orders endpoint, applying only the
   * filters that are provided. Filters are combinable: passing more than one
   * parameter narrows the search with an implicit AND.
   *
   * @param T The type of the response body expected from the GET request.
   * @param restaurantId Optional restaurant id filter.
   * @param customerId Optional user/customer id filter.
   * @param status Optional order status filter (e.g. "PENDING", "DELIVERED").
   * @return The HttpResponse containing the response from the server.
   */
  inline fun <reified T> httpGetOrders(
    restaurantId: String? = null,
    customerId: String? = null,
    status: String? = null,
  ): HttpResponse<T> {
    val builder = UriBuilder.of("")
    restaurantId?.let { builder.queryParam("restaurantId", it) }
    customerId?.let { builder.queryParam("customerId", it) }
    status?.let { builder.queryParam("status", it) }

    println("${baseUrl}${builder.build()}")

    return client.toBlocking().exchange(
      HttpRequest.GET<T>("${baseUrl}${builder.build()}"),
      T::class.java,
    )
  }

  /**
   * Makes an HTTP DELETE request to the discard order endpoint with the given request body.
   *
   * @param T The type of the response body expected from the DELETE request.
   * @param endPoint The endpoint to which the DELETE request is made.
   * @return The HttpResponse containing the response from the server.
   */
  inline fun <reified T> httpDelete(endPoint: String): HttpResponse<T> =
    client.toBlocking().exchange(
      HttpRequest.DELETE<T>("${baseUrl}$endPoint"),
      T::class.java,
    )
}
