package com.munchies.user.infrastructure.api

import com.munchies.user.infrastructure.api.dto.UserDTO

interface UserClient : UserApi<String, UserDTO> {
  override fun getUser(id: String): UserDTO
}
