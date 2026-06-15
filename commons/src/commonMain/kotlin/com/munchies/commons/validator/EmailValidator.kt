package com.munchies.commons.validator

import com.munchies.commons.domain.port.InputValidator
import com.munchies.commons.domain.port.InputValidatorResult
import com.munchies.commons.domain.port.InvalidInput
import com.munchies.commons.domain.port.ValidInput

class EmailValidator : InputValidator() {
  override fun validate(input: Any): InputValidatorResult {
    if (input !is String) return InvalidInput("Input is not a string")
    val email = (input as String)
    if (email.isEmpty()) return InvalidInput("Email can't be blank")
    return ValidInput
  }
}
