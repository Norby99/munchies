package com.munchies.suggestion.infrastructure.adapter.outbound.llm.codec

import com.munchies.suggestion.domain.model.SuggestionConfidence
import com.munchies.suggestion.domain.model.SuggestionResponse
import com.munchies.suggestion.domain.port.Codec.CodecResult.Companion.CodecSuccess
import com.munchies.suggestion.domain.port.Codec.CodecResult.Companion.MalformedCodecResult
import com.munchies.suggestion.domain.port.JsonParser
import com.munchies.suggestion.domain.port.JsonParser.JsonParserResult.Companion.JsonParserSuccess
import com.munchies.suggestion.domain.port.JsonParser.JsonParserResult.Companion.MalformedJsonParserResult
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class LLMCodecTest {

  @Test
  fun `Codec invokes parser when decoding raw`() {
    val parser = mock<JsonParser> {
      on { parse(any()) } doReturn MalformedJsonParserResult
    }

    val codec = LLMCodec(parser)
    codec.decode("raw response")

    verify(parser).parse(any())
  }

  @Test
  fun `Codec returns success when successfully decoding raw`() {
    val response = SuggestionResponse(
      rationale = "test rationale",
      confidence = SuggestionConfidence.HIGH,
      suggestedMenuItems = listOf(),
    )

    val parser = mock<JsonParser> {
      on { parse(any()) } doReturn JsonParserSuccess(response)
    }

    val codec = LLMCodec(parser)
    val result = codec.decode("raw response")

    result shouldBe CodecSuccess(response)
  }

  @Test
  fun `Codec returns failure when raw is malformed`() {
    val parser = mock<JsonParser> {
      on { parse(any()) } doReturn MalformedJsonParserResult
    }

    val codec = LLMCodec(parser)
    val result = codec.decode("raw response")

    result shouldBe MalformedCodecResult
  }

  @Test
  fun `Codec returns malformed when raw is malformed`() {
    val codec = LLMCodec()
    val result = codec.decode("raw response")

    result shouldBe MalformedCodecResult
  }

  @Test
  fun `Codec returns not-empty encoded raw`() {
    val codec = LLMCodec()
    val result = codec.encode(mock())

    result.shouldNotBeEmpty()
  }
}
