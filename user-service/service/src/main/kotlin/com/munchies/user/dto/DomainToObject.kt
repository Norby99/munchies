package com.munchies.user.dto

import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId
import com.munchies.user.infrastructure.api.dto.UserDTO

fun UserDTO.toDomain(): User = User(id = UserId(id))
fun User.toDTO(): UserDTO = UserDTO(id = id.value)
