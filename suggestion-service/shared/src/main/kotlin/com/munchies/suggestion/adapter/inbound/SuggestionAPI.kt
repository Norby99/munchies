package com.munchies.suggestion.adapter.inbound

interface SuggestionAPI<Request, Response> {
  fun suggestMenuItem(request: Request): Response
}
