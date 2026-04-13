package com.munchies.user.infrastructure.adapter.inbound.web.config

object UserServiceConfig {
  const val SERVICE_NAME = "user-service"
  const val SERVICE_PATH = "/users/"
  const val GET_USER_PATH = "{id}/"
  const val REGISTER_USER_PATH = "register/"
  const val LOGIN_USER_PATH = "login/"
  const val UPDATE_USER_PASSWORD_PATH = "update-password/"
  const val UPDATE_USER_INFO_PATH = "update-info/"
  const val SERVICE_PORT = 8080
}
