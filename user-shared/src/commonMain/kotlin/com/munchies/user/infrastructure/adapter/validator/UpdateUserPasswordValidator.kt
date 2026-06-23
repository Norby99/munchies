package com.munchies.user.infrastructure.adapter.validator

import com.munchies.commons.domain.port.InputValidator
import com.munchies.commons.domain.port.InputValidatorResult
import com.munchies.commons.domain.port.InvalidInput
import com.munchies.commons.domain.port.ValidInput
import com.munchies.user.infrastructure.adapter.inbound.request.UpdateUserPasswordRequest

class UpdateUserPasswordValidator : InputValidator<UpdateUserPasswordRequest>() {
  override fun validate(input: UpdateUserPasswordRequest): InputValidatorResult {
    if (input.newPassword.isEmpty()) return InvalidInput("New password cannot be empty")
    if (input.oldHashedPassword.isEmpty()) return InvalidInput("Old password cannot be empty")
    if (input.email.isEmpty() and input.username.isEmpty()) {
      return InvalidInput(
        "Both email and username cannot be empty",
      )
    }
    if (input.id.isEmpty()) {
      return InvalidInput(
        "UserId cannot be empty",
      )
    }
    return ValidInput
  }
}
