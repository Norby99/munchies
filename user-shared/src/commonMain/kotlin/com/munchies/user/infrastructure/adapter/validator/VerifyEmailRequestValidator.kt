package com.munchies.user.infrastructure.adapter.validator

import com.munchies.commons.domain.port.InputValidator
import com.munchies.commons.domain.port.InputValidatorResult
import com.munchies.commons.domain.port.InvalidInput
import com.munchies.commons.domain.port.ValidInput
import com.munchies.user.infrastructure.adapter.inbound.request.VerifyEmailRequest

class VerifyEmailRequestValidator : InputValidator<VerifyEmailRequest>() {
  override fun validate(input: VerifyEmailRequest): InputValidatorResult {
    if (input.id.isEmpty()) return InvalidInput("UserId cannot be empty")
    if (input.otk.isEmpty()) return InvalidInput("One Time Token cannot be empty")
    return ValidInput
  }
}
