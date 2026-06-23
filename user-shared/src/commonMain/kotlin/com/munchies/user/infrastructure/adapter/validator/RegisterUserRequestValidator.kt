package com.munchies.user.infrastructure.adapter.validator

import com.munchies.commons.domain.port.InputValidator
import com.munchies.commons.domain.port.InputValidatorResult
import com.munchies.commons.domain.port.InvalidInput
import com.munchies.commons.domain.port.ValidInput
import com.munchies.user.infrastructure.adapter.inbound.request.RegisterUserRequest

class RegisterUserRequestValidator : InputValidator<RegisterUserRequest>() {
  override fun validate(input: RegisterUserRequest): InputValidatorResult {
    if (input.hashedPassword.isEmpty()) return InvalidInput("Password field cannot be empty")
    if (input.saltValue.isEmpty()) return InvalidInput("Salt field cannot be empty")
    val userDTO = input.user
    if (userDTO.username.isEmpty()) return InvalidInput("Username cannot be empty")
    if (userDTO.role.isEmpty()) return InvalidInput("Role cannot be empty")
    if (userDTO.email.isEmpty()) return InvalidInput("Email cannot be empty")
    return ValidInput
  }
}
