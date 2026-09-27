package com.munchies.suggestion.infrastructure.adapter.inbound

import com.munchies.suggestion.infrastructure.adapter.inbound.request.SuggestItemRequest

interface SuggestionAPI< Response> {
  fun suggestMenuItem(request: SuggestItemRequest): Response
}
