package com.munchies.suggestion.domain.model

import com.munchies.commons.UUIDEntityId

data class SuggestionRequestId(override val value: String = newId()) : UUIDEntityId(value)
