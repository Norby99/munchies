package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.infrastructure.adapter.dto.OrderDto
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient

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
   * @param request The request body to be sent in the POST request.
   * @param endPoint The endpoint to which the POST request is made.
   * @return The HttpResponse containing the response from the server.
   */
  fun httpPost(request: Any, endPoint: String): HttpResponse<String> = client.toBlocking().exchange(
    HttpRequest.POST(
      "${baseUrl}$endPoint",
      request,
    ),
    String::class.java,
  )

  /**
   * Makes an HTTP PATCH request to the specified endpoint with the given request body.
   *
   * @param request The request body to be sent in the PATCH request.
   * @param endPoint The endpoint to which the PATCH request is made.
   * @return The HttpResponse containing the response from the server.
   */
  fun httpPatch(request: Any, endPoint: String): HttpResponse<String> =
    client.toBlocking().exchange(
      HttpRequest.PATCH(
        "${baseUrl}$endPoint",
        request,
      ),
      String::class.java,
    )

  /**
   * Makes an HTTP GET request to the specified endpoint.
   *
   * @param endPoint The endpoint to which the GET request is made.
   * @return The HttpResponse containing the response from the server, which is expected to be
   * of type OrderDto.Takeaway.
   */
  fun httpGet(endPoint: String): HttpResponse<OrderDto.Takeaway> = client.toBlocking().exchange(
    HttpRequest.GET<Any>("${baseUrl}$endPoint"),
    OrderDto.Takeaway::class.java,
  )

  /**
   * Makes an HTTP DELETE request to the discard order endpoint with the given request body.
   *
   * @param endPoint The endpoint to which the DELETE request is made.
   * @return The HttpResponse containing the response from the server.
   */
  fun httpDelete(endPoint: String): HttpResponse<String> = client.toBlocking().exchange(
    HttpRequest.DELETE<Any>("${baseUrl}$endPoint"),
    String::class.java,
  )
}
