package com.munchies.user.presentation

import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId
import com.munchies.user.presentation.dto.UserDTO

fun UserDTO.toDomain(): User = User(id = UserId(id))
fun User.toDTO(): UserDTO = UserDTO(id = id.value)
