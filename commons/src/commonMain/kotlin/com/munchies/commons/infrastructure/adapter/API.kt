package com.munchies.commons.infrastructure.adapter

import com.munchies.commons.domain.port.AuthRole
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JsExport
@Serializable
@SerialName("JsonEncodable")
abstract class JsonEncodable {
  abstract fun toJson(): String
}

@JsExport
@Serializable
@SerialName("WebResponse")
abstract class WebResponse<Result> : JsonEncodable() {
  abstract val result: Result
  abstract val code: Int
}

@JsExport
@Serializable
@SerialName("API")
abstract class API<
  Request : JsonEncodable,
  Response : WebResponse<Result>,
  Result,
  Success : Result,
  Failure : Result,
  > {
  abstract fun getPath(): String
  abstract fun getPort(): Int
  abstract fun getMethod(): HttpMethod
  abstract fun getRequiredAuthRole(): AuthRole

  abstract fun parseRequest(json: String): Request
  abstract fun parseResponse(json: String): Response

  abstract fun generateFailure(reason: String): Failure
  abstract fun generateResponse(result: Result, code: Int): Response
}
