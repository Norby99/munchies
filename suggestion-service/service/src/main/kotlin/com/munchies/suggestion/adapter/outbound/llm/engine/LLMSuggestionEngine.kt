package com.munchies.suggestion.adapter.outbound.llm.engine

import com.munchies.suggestion.adapter.outbound.llm.codec.LLMCodec
import com.munchies.suggestion.adapter.outbound.llm.config.ModelConstants.DEFAULT_MODEL
import com.munchies.suggestion.domain.model.SuggestionRequest
import com.munchies.suggestion.domain.port.Codec
import com.munchies.suggestion.domain.port.Codec.CodecResult.Companion.CodecSuccess
import com.munchies.suggestion.domain.port.Codec.CodecResult.Companion.MalformedCodecResult
import com.munchies.suggestion.domain.port.SuggestionEngine
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.EmptySuggestion
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.MalformedSuggestion
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.SuggestionSuccess
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.TimeoutSuggestion
import dev.langchain4j.model.chat.ChatModel

class LLMSuggestionEngine(
  private val codec: Codec<SuggestionRequest> = LLMCodec(),
  private val model: ChatModel = DEFAULT_MODEL,
) : SuggestionEngine {

  override fun suggest(request: SuggestionRequest): SuggestionEngine.SuggestionResult {
    val encodedRequest = codec.encode(request)
    val rawResponse = model.chat(encodedRequest)
    if (rawResponse.isNullOrEmpty()) return EmptySuggestion
    return try {
      when (val decodedResponse = codec.decode(rawResponse)) {
        is MalformedCodecResult -> MalformedSuggestion
        is CodecSuccess -> SuggestionSuccess(decodedResponse.result)
      }
    } catch (_: Exception) {
      TimeoutSuggestion
    }
  }
}
