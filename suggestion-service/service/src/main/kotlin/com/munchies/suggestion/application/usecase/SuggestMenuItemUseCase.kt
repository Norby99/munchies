package com.munchies.suggestion.application.usecase

import com.munchies.suggestion.application.port.inbound.SuggestMenuItem
import com.munchies.suggestion.domain.port.SuggestionEngine

class SuggestMenuItemUseCase(override val engine: SuggestionEngine) : SuggestMenuItem
