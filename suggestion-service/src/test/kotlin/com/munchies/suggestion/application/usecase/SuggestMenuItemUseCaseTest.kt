package com.munchies.suggestion.application.usecase

import com.munchies.commons.UUIDEntityId
import com.munchies.suggestion.domain.model.SuggestionConfidence
import com.munchies.suggestion.domain.model.SuggestionRequest
import com.munchies.suggestion.domain.model.SuggestionRequestId
import com.munchies.suggestion.domain.model.SuggestionResponse
import com.munchies.suggestion.domain.port.SuggestionEngine
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.SuggestionSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class SuggestMenuItemUseCaseTest {

  @Test
  fun `getEngine returns engine`() {
    val engine = mock<SuggestionEngine>()
    val useCase = SuggestMenuItemUseCase(engine)
    useCase.engine shouldBe engine
  }

  @Test
  fun `execute delegates to engine suggest`() {
    val request = SuggestionRequest(
      id = SuggestionRequestId(""),
      user = UUIDEntityId(""),
      menu = listOf(),
      userPreferences = listOf(),
    )
    val response = SuggestionResponse(
      rationale = "test",
      confidence = SuggestionConfidence.HIGH,
      suggestedMenuItems = listOf(),
    )
    val engine: SuggestionEngine = mock {
      on { suggest(request) } doReturn SuggestionSuccess(response)
    }
    val useCase = SuggestMenuItemUseCase(engine)
    val result = useCase.execute(request)

    result.shouldBeInstanceOf<SuggestionSuccess>().result shouldBe response
    verify(engine).suggest(request)
  }
}
