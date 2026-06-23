package com.munchies.user.infrastructure.adapter.validator

import com.munchies.commons.domain.port.InputValidator
import com.munchies.commons.domain.port.InputValidatorResult
import com.munchies.commons.domain.port.InvalidInput
import com.munchies.commons.domain.port.ValidInput
import com.munchies.user.infrastructure.adapter.dto.UserDTO

class UserDTOValidator : InputValidator<UserDTO>() {
  override fun validate(input: UserDTO): InputValidatorResult {
    if (input.username.isEmpty()) return InvalidInput("Username cannot be empty")
    if (input.role.isEmpty()) return InvalidInput("Role cannot be empty")
    if (input.email.isEmpty()) return InvalidInput("Email cannot be empty")
    if (input.id.isEmpty()) return InvalidInput("Id cannot be empty")
    return ValidInput
  }
}
