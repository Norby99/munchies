package com.munchies.user.infrastructure.adapter.dto.mapper

import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId
import com.munchies.user.infrastructure.adapter.dto.UserDTO

fun UserDTO.toDomain(): User = User(id = UserId(id))
fun User.toDTO(): UserDTO = UserDTO(id = id.value)
