package com.munchies.suggestion.application.usecase

import com.munchies.commons.UUIDEntityId
import com.munchies.suggestion.domain.model.SuggestionConfidence
import com.munchies.suggestion.domain.model.SuggestionRequest
import com.munchies.suggestion.domain.model.SuggestionRequestId
import com.munchies.suggestion.domain.model.SuggestionResponse
import com.munchies.suggestion.domain.port.SuggestionEngine
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.SuggestionSuccess
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.TimeoutSuggestion
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class MultipleAttemptSuggestMenuItemUseCaseTest {
  @Test
  fun execute() {
  }

  @Test
  fun getEngine() {
    val engine = mock<SuggestionEngine>()

    MultipleAttemptSuggestMenuItemUseCase(engine).engine shouldBe engine
  }

  @Test
  fun `engine receives correct max attemps`(){

    shouldNotThrow<AssertionError>{ MultipleAttemptSuggestMenuItemUseCase(mock(), 10) }
    shouldThrow<AssertionError> { MultipleAttemptSuggestMenuItemUseCase(mock(), 0) }
    shouldThrow<AssertionError> { MultipleAttemptSuggestMenuItemUseCase(mock(), -1) }

  }

  val request = SuggestionRequest(
    id = SuggestionRequestId(""),
    user = UUIDEntityId(""),
    menu = listOf(),
    userPreferences = listOf()
    )


  @Test
  fun `engine returns after one attempt when correct`(){

    val response = SuggestionResponse(
      rationale = "",
      confidence = SuggestionConfidence.HIGH,
      suggestedMenuItems = listOf()
    )

    val correctEngine: SuggestionEngine = mock {
       on { suggest(request) } doReturn SuggestionSuccess(response)
    }

    val useCase = MultipleAttemptSuggestMenuItemUseCase(correctEngine, 3)

    useCase.execute(request).shouldBeInstanceOf<SuggestionSuccess>()
      .result.shouldBe(response)
    verify(correctEngine).suggest(request)
  }


  @Test
  fun `engine returns timeout when runs out of attempts`(){

    val timeoutEngine: SuggestionEngine = mock {
      on { suggest(request) } doReturn TimeoutSuggestion
    }

    val baseUseCase = SuggestMenuItemUseCase(timeoutEngine)

    val useCase = MultipleAttemptSuggestMenuItemUseCase(timeoutEngine, 3, baseUseCase)

    useCase.execute(request).shouldBeInstanceOf<TimeoutSuggestion>()
    verify(timeoutEngine, times(3)).suggest(request)
  }

}
