package com.munchies.suggestion.infrastructure.adapter.inbound.web.config

import com.munchies.suggestion.domain.port.SuggestionEngine
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class SuggestionBeansTest {

  @Test
  fun `getSuggestionMenuItem creates SuggestMenuItem bean`() {
    val engine = mock<SuggestionEngine>()
    val beans = SuggestionBeans()
    val service = beans.getSuggestionMenuItem(engine)
    service shouldNotBe null
    service.engine shouldBe engine
  }
}
