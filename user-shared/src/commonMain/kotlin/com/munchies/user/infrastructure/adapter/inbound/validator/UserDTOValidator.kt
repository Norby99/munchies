package com.munchies.user.infrastructure.adapter.inbound.validator

import com.munchies.commons.domain.port.InputValidator
import com.munchies.commons.domain.port.InputValidatorResult
import com.munchies.commons.domain.port.InvalidInput
import com.munchies.commons.domain.port.ValidInput
import com.munchies.user.infrastructure.adapter.dto.UserDTO

class UserDTOValidator : InputValidator() {
  override fun validate(input: Any): InputValidatorResult {
    if (input !is UserDTO) return InvalidInput("input is not UserDTO")
    val userDTO = (input as UserDTO)
    if (userDTO.username.isEmpty()) return InvalidInput("Username cannot be empty")
    if (userDTO.role.isEmpty()) return InvalidInput("Role cannot be empty")
    if (userDTO.email.isEmpty()) return InvalidInput("Email cannot be empty")
    if (userDTO.id.isEmpty()) return InvalidInput("Id cannot be empty")
    return ValidInput
  }
}
