package com.munchies.commons.domain.port

import kotlin.js.JsExport
@JsExport
abstract class InputValidator {
  abstract fun validate(input: Any): InputValidatorResult
}

@JsExport
interface InputValidatorResult

@JsExport
data object ValidInput : InputValidatorResult

@JsExport
data class InvalidInput(val reason: String = "") : InputValidatorResult
