package com.munchies.user.infrastructure.adapter.validator

import com.munchies.commons.domain.port.InputValidator
import com.munchies.commons.domain.port.InputValidatorResult
import com.munchies.commons.domain.port.InvalidInput
import com.munchies.commons.domain.port.ValidInput
import com.munchies.user.infrastructure.adapter.inbound.request.LoginUserRequest

class LoginUserRequestValidator : InputValidator<LoginUserRequest>() {
  override fun validate(input: LoginUserRequest): InputValidatorResult {
    if (input.email.isEmpty() and input.username.isEmpty()) {
      return InvalidInput(
        "Both email and username cannot be empty",
      )
    }
    if (input.password.isEmpty()) return InvalidInput("Password cannot be empty")
    return ValidInput
  }
}
