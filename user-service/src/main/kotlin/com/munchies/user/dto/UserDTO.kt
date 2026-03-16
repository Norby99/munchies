package com.munchies.user.dto

import com.munchies.user.domain.User
import com.munchies.user.domain.UserId
import io.micronaut.serde.annotation.Serdeable

@Serdeable
class UserDTO(val id: String)

fun UserDTO.toDomain(): User = User(id = UserId(id))
fun User.toDTO(): UserDTO = UserDTO(id = id.value)
