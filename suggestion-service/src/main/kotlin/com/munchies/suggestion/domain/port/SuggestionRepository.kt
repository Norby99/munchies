package com.munchies.suggestion.domain.port

import com.munchies.commons.Repository
import com.munchies.suggestion.domain.model.SuggestionRequest
import com.munchies.suggestion.domain.model.SuggestionRequestId

interface SuggestionRepository : Repository<SuggestionRequestId, SuggestionRequest>
