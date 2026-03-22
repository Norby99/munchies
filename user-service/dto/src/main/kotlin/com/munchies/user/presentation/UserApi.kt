package com.munchies.user.presentation

interface UserApi<IdType, E> {
  fun getUser(id: IdType): E
}
