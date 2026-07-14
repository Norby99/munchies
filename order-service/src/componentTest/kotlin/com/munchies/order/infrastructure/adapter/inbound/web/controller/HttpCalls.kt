package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.infrastructure.adapter.dto.OrderDto
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient

class HttpCalls(val baseUrl: String, val client: HttpClient) {

  fun httpPost(request: Any, endPoint: String): HttpResponse<String> = client.toBlocking().exchange(
    HttpRequest.POST(
      "${baseUrl}$endPoint",
      request,
    ),
    String::class.java,
  )

  fun httpPatch(request: Any, endPoint: String): HttpResponse<String> =
    client.toBlocking().exchange(
      HttpRequest.PATCH(
        "${baseUrl}$endPoint",
        request,
      ),
      String::class.java,
    )

  fun httpGet(endPoint: String): HttpResponse<OrderDto.Takeaway> = client.toBlocking().exchange(
    HttpRequest.GET<Any>("${baseUrl}$endPoint"),
    OrderDto.Takeaway::class.java,
  )

  fun httpDelete(request: String): HttpResponse<String> = client.toBlocking().exchange(
    HttpRequest.DELETE("${baseUrl}${OrderServiceConfig.DISCARD_ORDER_PATH}", request),
    String::class.java,
  )
}
