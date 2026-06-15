package com.munchies.user.infrastructure.adapter.inbound.validator

import com.munchies.commons.domain.port.InputValidator
import com.munchies.commons.domain.port.InputValidatorResult
import com.munchies.commons.domain.port.InvalidInput
import com.munchies.commons.domain.port.ValidInput
import com.munchies.user.infrastructure.adapter.inbound.request.UpdateUserPasswordRequest

class UpdateUserPasswordValidator : InputValidator() {
  override fun validate(input: Any): InputValidatorResult {
    if (input !is UpdateUserPasswordRequest) {
      return InvalidInput(
        "input is not UpdateUserPasswordRequest",
      )
    }
    val request = (input as UpdateUserPasswordRequest)
    if (request.newPassword.isEmpty()) return InvalidInput("New password cannot be empty")
    if (request.oldHashedPassword.isEmpty()) return InvalidInput("Old password cannot be empty")
    val user = request.user
    if (user.email.isEmpty() and user.username.isEmpty()) {
      return InvalidInput(
        "Both email and username cannot be empty",
      )
    }
    return ValidInput
  }
}
