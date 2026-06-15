package com.munchies.user.infrastructure.adapter.inbound.validator

import com.munchies.commons.domain.port.InputValidator
import com.munchies.commons.domain.port.InputValidatorResult
import com.munchies.commons.domain.port.InvalidInput
import com.munchies.commons.domain.port.ValidInput
import com.munchies.user.infrastructure.adapter.inbound.request.LoginUserRequest

class LoginUserRequestValidator : InputValidator() {
  override fun validate(input: Any): InputValidatorResult {
    if (input !is LoginUserRequest) return InvalidInput("input is not LoginUserRequest")
    val request = (input as LoginUserRequest)
    if (request.email.isEmpty() and request.username.isEmpty()) {
      return InvalidInput(
        "Both email and username cannot be empty",
      )
    }
    if (request.password.isEmpty()) return InvalidInput("Password cannot be empty")
    return ValidInput
  }
}
