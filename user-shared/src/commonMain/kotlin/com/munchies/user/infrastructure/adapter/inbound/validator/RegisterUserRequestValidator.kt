package com.munchies.user.infrastructure.adapter.inbound.validator

import com.munchies.commons.domain.port.InputValidator
import com.munchies.commons.domain.port.InputValidatorResult
import com.munchies.commons.domain.port.InvalidInput
import com.munchies.commons.domain.port.ValidInput
import com.munchies.user.infrastructure.adapter.inbound.request.RegisterUserRequest

class RegisterUserRequestValidator : InputValidator() {
  override fun validate(input: Any): InputValidatorResult {
    if (input !is RegisterUserRequest) return InvalidInput("input is not RegisterUserRequest")
    val request = (input as RegisterUserRequest)
    if (request.hashedPassword.isEmpty()) return InvalidInput("Password field cannot be empty")
    if (request.saltValue.isEmpty()) return InvalidInput("Salt field cannot be empty")
    val userDTO = request.user
    if (userDTO.username.isEmpty()) return InvalidInput("Username cannot be empty")
    if (userDTO.role.isEmpty()) return InvalidInput("Role cannot be empty")
    if (userDTO.email.isEmpty()) return InvalidInput("Email cannot be empty")
    return ValidInput
  }
}
