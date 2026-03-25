package com.munchies.user.domain.model

import com.munchies.commons.AggregateRoot

data class User(override val id: UserId) : AggregateRoot<UserId>(id = id)
