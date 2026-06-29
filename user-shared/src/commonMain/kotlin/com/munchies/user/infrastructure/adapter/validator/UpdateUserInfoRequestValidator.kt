package com.munchies.user.infrastructure.adapter.validator

import com.munchies.commons.domain.port.InputValidator
import com.munchies.commons.domain.port.InputValidatorResult
import com.munchies.user.infrastructure.adapter.inbound.request.UpdateUserInfoRequest

class UpdateUserInfoRequestValidator : InputValidator<UpdateUserInfoRequest>() {
  override fun validate(input: UpdateUserInfoRequest): InputValidatorResult {
    return UserDTOValidator().validate(input.user)
  }
}
