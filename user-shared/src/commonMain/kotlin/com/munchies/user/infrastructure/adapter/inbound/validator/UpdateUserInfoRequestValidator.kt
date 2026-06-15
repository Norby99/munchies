package com.munchies.user.infrastructure.adapter.inbound.validator

import com.munchies.commons.domain.port.InputValidator
import com.munchies.commons.domain.port.InputValidatorResult
import com.munchies.commons.domain.port.InvalidInput
import com.munchies.user.infrastructure.adapter.inbound.request.UpdateUserInfoRequest

class UpdateUserInfoRequestValidator : InputValidator() {
  override fun validate(input: Any): InputValidatorResult {
    if (input !is UpdateUserInfoRequest) return InvalidInput("input is not UpdateUserInfoRequest")
    val request = (input as UpdateUserInfoRequest)
    return UserDTOValidator().validate(request.user)
  }
}
