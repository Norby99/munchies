package com.munchies.user.dto

import com.munchies.user.api.UserDTO
import com.munchies.user.domain.User
import com.munchies.user.domain.UserId

fun UserDTO.toDomain(): User = User(id = UserId(id))
fun User.toDTO(): UserDTO = UserDTO(id = id.value)
