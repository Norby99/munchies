package com.munchies.suggestion.infrastructure.adapter.inbound.web.controller

import com.munchies.suggestion.application.port.inbound.SuggestMenuItem
import com.munchies.suggestion.domain.port.SuggestionEngine
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.EmptySuggestion
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.MalformedSuggestion
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.SuggestionSuccess
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.TimeoutSuggestion
import com.munchies.suggestion.infrastructure.adapter.dto.SuggestionConfidenceDTO
import com.munchies.suggestion.infrastructure.adapter.dto.SuggestionRequestDTO
import com.munchies.suggestion.infrastructure.adapter.dto.SuggestionResponseDTO
import com.munchies.suggestion.infrastructure.adapter.dto.mapper.SuggestionRequestMapper.toDomain
import com.munchies.suggestion.infrastructure.adapter.dto.mapper.SuggestionResponseMapper.toDomain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.serde.annotation.SerdeImport
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findAnnotations
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class SuggestionControllerTest {

  private fun getController(suggestMenuItem: SuggestMenuItem = mock()) =
    SuggestionController(suggestMenuItem)

  private fun getSuggestMenuItem(engine: SuggestionEngine = mock()): SuggestMenuItem {
    return object : SuggestMenuItem {
      override val engine: SuggestionEngine
        get() = engine
    }
  }

  @Test
  fun `controller is @Controller annotated`() {
    val controller = SuggestionController::class
    controller.findAnnotation<Controller>() shouldNotBe null
  }

  @Test
  fun `controller should have @SerdeImport for DTOs`() {
    val controller = SuggestionController::class
    val serdeImport = controller.findAnnotations<SerdeImport>()
    serdeImport shouldNotBe null
    serdeImport.shouldNotBeEmpty()
  }

  @Test
  fun `controller should invoke engine when prompted to suggest menu item`() {
    val dto = SuggestionRequestDTO(
      userId = "user-id",
      menu = listOf(),
      userPreferences = listOf(),
    )

    val suggestMenuItem = mock<SuggestMenuItem> {
      on { execute(dto.toDomain()) } doReturn MalformedSuggestion
    }
    val controller = getController(suggestMenuItem = suggestMenuItem)

    controller.suggestMenuItem(dto)

    Mockito.verify(suggestMenuItem).execute(any())
  }

  @Test
  fun `controller should return unprocessable entity when engine returns malformed suggestion`() {
    val dto = SuggestionRequestDTO(
      userId = "user-id",
      menu = listOf(),
      userPreferences = listOf(),
    )

    val suggestMenuItem = getSuggestMenuItem(
      engine = mock {
        on {
          suggest(
            any(),
          )
        } doReturn MalformedSuggestion
      },
    )
    val controller = getController(suggestMenuItem = suggestMenuItem)

    val response = controller.suggestMenuItem(dto)
    response.status shouldBe HttpStatus.UNPROCESSABLE_ENTITY
    response.body() shouldBe null
  }

  @Test
  fun `controller should return no content when engine returns empty suggestion`() {
    val dto = SuggestionRequestDTO(
      userId = "user-id",
      menu = listOf(),
      userPreferences = listOf(),
    )

    val suggestMenuItem = getSuggestMenuItem(
      engine = mock {
        on {
          suggest(
            any(),
          )
        } doReturn EmptySuggestion
      },
    )

    val controller = getController(suggestMenuItem = suggestMenuItem)

    val response = controller.suggestMenuItem(dto)
    response.status shouldBe HttpStatus.NO_CONTENT
    response.body() shouldBe null
  }

  @Test
  fun `controller should return server error when engine returns timeout`() {
    val dto = SuggestionRequestDTO(
      userId = "user-id",
      menu = listOf(),
      userPreferences = listOf(),
    )

    val suggestMenuItem = getSuggestMenuItem(
      engine = mock {
        on {
          suggest(
            any(),
          )
        } doReturn TimeoutSuggestion
      },
    )

    val controller = getController(suggestMenuItem = suggestMenuItem)

    val response = controller.suggestMenuItem(dto)

    response.status shouldBe HttpStatus.INTERNAL_SERVER_ERROR
    response.body() shouldBe null
  }

  @Test
  fun `controller should return ok when engine returns success`() {
    val requestDTO = SuggestionRequestDTO(
      userId = "user-id",
      menu = listOf(),
      userPreferences = listOf(),
    )

    val responseDTO = SuggestionResponseDTO(
      rationale = "",
      confidence = SuggestionConfidenceDTO("HIGH"),
      suggestedMenuItems = listOf(),
    )

    val response = responseDTO.toDomain()

    val suggestMenuItem = getSuggestMenuItem(
      engine = mock {
        on {
          suggest(
            any(),
          )
        } doReturn SuggestionSuccess(response)
      },
    )
    val controller = getController(suggestMenuItem = suggestMenuItem)

    val res = controller.suggestMenuItem(requestDTO)
    res.status shouldBe HttpStatus.OK
    res.body() shouldNotBe null
    res.body() shouldBe responseDTO
  }
}
