package com.munchies.user.infrastructure.adapter.inbound.validator

import com.munchies.commons.domain.port.InputValidator
import com.munchies.commons.domain.port.InputValidatorResult
import com.munchies.commons.domain.port.InvalidInput
import com.munchies.commons.domain.port.ValidInput
import com.munchies.user.infrastructure.adapter.inbound.request.DeleteUserRequest

class DeleteUserRequestValidator : InputValidator() {
  override fun validate(input: Any): InputValidatorResult {
    if (input !is DeleteUserRequest) return InvalidInput("Input is not DeleteUserRequest")
    val request = (input as DeleteUserRequest)
    if (request.userId.isEmpty()) return InvalidInput("UserId cannot be empty")
    return ValidInput
  }
}
