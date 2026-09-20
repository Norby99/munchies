package com.munchies.suggestion.domain.port
import com.munchies.suggestion.domain.model.SuggestionResponse
import com.munchies.suggestion.domain.port.JsonParser.JsonParserResult.Companion.JsonParserSuccess

interface Codec<Input> {
  val parser: JsonParser

  fun decode(raw: String): CodecResult = when (val res = parser.parse(raw)) {
    is JsonParserSuccess -> CodecResult.Companion.CodecSuccess(res.result)
    else -> CodecResult.Companion.MalformedCodecResult
  }

  fun encode(request: Input): String = request.render()

  fun Input.render(): String

  sealed interface CodecResult {
    companion object {
      data class CodecSuccess(val result: SuggestionResponse) : CodecResult
      data object MalformedCodecResult : CodecResult
    }
  }
}
