package com.munchies.user.infrastructure.adapter.inbound.validator

import com.munchies.commons.domain.port.InputValidator
import com.munchies.commons.domain.port.InputValidatorResult
import com.munchies.commons.domain.port.InvalidInput
import com.munchies.commons.domain.port.ValidInput
import com.munchies.user.infrastructure.adapter.inbound.request.VerifyEmailRequest

class VerifyEmailRequestValidator : InputValidator() {
  override fun validate(input: Any): InputValidatorResult {
    if (input !is VerifyEmailRequest) return InvalidInput("input is not VerifyEmailRequest")
    val request = (input as VerifyEmailRequest)
    if (request.id.isEmpty()) return InvalidInput("UserId cannot be empty")
    if (request.otk.isEmpty()) return InvalidInput("One Time Token cannot be empty")
    return ValidInput
  }
}
