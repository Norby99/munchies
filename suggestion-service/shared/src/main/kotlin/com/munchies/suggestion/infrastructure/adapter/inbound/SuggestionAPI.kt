package com.munchies.suggestion.infrastructure.adapter.inbound

interface SuggestionAPI<Request, Response> {
  fun suggestMenuItem(request: Request): Response
}
