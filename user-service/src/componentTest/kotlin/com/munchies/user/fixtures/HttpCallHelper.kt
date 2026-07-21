package com.munchies.user.fixtures

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient

class HttpCallHelper(val baseUrl: String, val client: HttpClient) {

  fun post(request: Any, endPoint: String): HttpResponse<String> = client.toBlocking()
    .exchange(
      HttpRequest.POST(
        "${baseUrl}$endPoint",
        request,
      ),
      String::class.java,
    )

  fun get(endPoint: String): HttpResponse<String> = client.toBlocking()
    .exchange(
      HttpRequest.GET<Any>(
        "${baseUrl}$endPoint",
      ),
      String::class.java,
    )

  fun patch(request: String, endPoint: String): HttpResponse<String> = client.toBlocking()
    .exchange(
      HttpRequest.PATCH<Any>(
        "${baseUrl}$endPoint",
        request,
      ),
      String::class.java,
    )

  fun delete(endPoint: String): HttpResponse<String> = client.toBlocking()
    .exchange(
      HttpRequest.DELETE<Any>(
        "${baseUrl}$endPoint",
      ),
      String::class.java,
    )
}
