package com.munchies.commons.validator

import com.munchies.commons.domain.port.InputValidator
import com.munchies.commons.domain.port.InputValidatorResult
import com.munchies.commons.domain.port.InvalidInput
import com.munchies.commons.domain.port.ValidInput

class EmailValidator : InputValidator<String>() {
  override fun validate(input: String): InputValidatorResult {
    if (input.isEmpty()) return InvalidInput("Email can't be blank")
    return ValidInput
  }
}
