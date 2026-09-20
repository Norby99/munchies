package com.munchies.suggestion.domain.model

import com.munchies.commons.UUIDEntityId

data class MenuItemId(override val value: String = newId()) : UUIDEntityId(value)
