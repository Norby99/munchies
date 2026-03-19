package com.munchies.user.domain.model

import com.munchies.commons.AggregateRoot

open class User(override val id: UserId) : AggregateRoot<UserId>(id = id)
