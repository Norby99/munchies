package com.munchies.suggestion.infrastructure.adapter.outbound.llm.engine

import com.munchies.suggestion.domain.model.MenuItem
import com.munchies.suggestion.domain.model.MenuItemId
import com.munchies.suggestion.domain.model.SuggestedMenuItem
import com.munchies.suggestion.domain.model.SuggestionConfidence
import com.munchies.suggestion.domain.model.SuggestionRequest
import com.munchies.suggestion.domain.model.SuggestionRequestId
import com.munchies.suggestion.domain.model.SuggestionResponse
import com.munchies.suggestion.domain.model.UserPreference
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.EmptySuggestion
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.MalformedSuggestion
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.SuggestionSuccess
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.TimeoutSuggestion
import com.munchies.suggestion.infrastructure.adapter.outbound.llm.codec.LLMCodec
import dev.langchain4j.model.chat.ChatModel
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class LLMSuggestionEngineTest {
  private val fakeRequest = SuggestionRequest(
    user = SuggestionRequestId("test-id"),
    menu = listOf(
      MenuItem(
        id = MenuItemId(),
        name = "Pizza",
        description = "Delicious cheese pizza",
        price = 9.99,
      ),
    ),
    userPreferences = listOf(UserPreference("likes spicy food")),
  )

  val codec = LLMCodec()

  val encodedRequest = codec.encode(fakeRequest)

  @Test
  fun `Engine returns empty suggestion when model doesnt answer`() {
    val model = mock<ChatModel> {
      on { chat(encodedRequest) } doReturn ""
    }

    val engine = LLMSuggestionEngine(
      model = model,
    )

    val result = engine.suggest(fakeRequest)

    verify(model).chat(encodedRequest)
    result shouldBe EmptySuggestion
  }

  @Test
  fun `Engine returns malformed suggestion when model malforms answer`() {
    val model = mock<ChatModel> {
      on { chat(encodedRequest) } doReturn "malformed response"
    }

    val engine = LLMSuggestionEngine(
      model = model,
    )

    val result = engine.suggest(fakeRequest)

    verify(model).chat(encodedRequest)
    result shouldBe MalformedSuggestion
  }

  @Test
  fun `Engine returns timeout when model isnt reachable`() {
    val model = mock<ChatModel> {
      on { chat(encodedRequest) } doThrow RuntimeException("Model not reachable")
    }

    val engine = LLMSuggestionEngine(
      model = model,
    )

    val result = engine.suggest(fakeRequest)

    verify(model).chat(encodedRequest)
    result shouldBe TimeoutSuggestion
  }

  @Test
  fun `Engine returns correct suggestion when model correctly answers`() {
    val suggestionText = """
      {
        "rationale": "Because it's a good idea",
        "confidence": "HIGH",
        "suggestedMenuItems": [
          {
            "itemId": "test-item-id",
            "reason": "Pizza is gud"
          }
        ]
      }
    """.trimIndent()

    val suggestionResponse = SuggestionResponse(
      rationale = "Because it's a good idea",
      confidence = SuggestionConfidence.HIGH,
      suggestedMenuItems = listOf(
        SuggestedMenuItem(
          itemId = "test-item-id",
          reason = "Pizza is gud",
        ),
      ),
    )

    val model = mock<ChatModel> {
      on { chat(encodedRequest) } doReturn suggestionText
    }

    val engine = LLMSuggestionEngine(
      model = model,
    )

    val result = engine.suggest(fakeRequest)

    verify(model).chat(encodedRequest)
    result shouldBe SuggestionSuccess(suggestionResponse)
  }
}
