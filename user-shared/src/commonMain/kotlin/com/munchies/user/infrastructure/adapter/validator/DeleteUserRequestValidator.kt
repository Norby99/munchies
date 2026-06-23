package com.munchies.user.infrastructure.adapter.validator

import com.munchies.commons.domain.port.InputValidator
import com.munchies.commons.domain.port.InputValidatorResult
import com.munchies.commons.domain.port.InvalidInput
import com.munchies.commons.domain.port.ValidInput
import com.munchies.user.infrastructure.adapter.inbound.request.DeleteUserRequest

class DeleteUserRequestValidator : InputValidator<DeleteUserRequest>() {
  override fun validate(input: DeleteUserRequest): InputValidatorResult {
    if (input.userId.isEmpty()) return InvalidInput("UserId cannot be empty")
    return ValidInput
  }
}
