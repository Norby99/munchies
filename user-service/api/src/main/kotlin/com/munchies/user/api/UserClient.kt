package com.munchies.user.api

interface UserClient : UserApi<String, UserDTO> {
  override fun getUser(id: String): UserDTO
}
