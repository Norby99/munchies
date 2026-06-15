package com.munchies.commons.domain.port

import kotlin.js.JsExport
@JsExport
abstract class InputValidator {
  abstract fun validate(input: Any): InputValidatorResult
}

@JsExport
interface InputValidatorResult
