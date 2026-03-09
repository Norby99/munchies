package com.munchies.user.api

import com.munchies.user.model.AuthResponse
import com.munchies.user.model.MePutRequest
import com.munchies.user.model.RegisterPostRequest
import com.munchies.user.model.User
import io.micronaut.http.HttpResponse
import reactor.core.publisher.Mono

interface AccountHTTPApi : AccountApi {
  override fun confirmEmailGet(token: String): Mono<Void> {
    TODO("Not yet implemented")
  }

  override fun meGet(): Mono<User> {
    TODO("Not yet implemented")
  }

  override fun mePut(mePutRequest: MePutRequest): Mono<Void> {
    TODO("Not yet implemented")
  }

  override fun registerPost(
    registerPostRequest: RegisterPostRequest,
  ): Mono<HttpResponse<AuthResponse>> {
    TODO("Not yet implemented")
  }
}
