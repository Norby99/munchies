package com.munchies.commons.domain.port

import kotlin.js.JsExport
@JsExport
abstract class InputValidator<I> {
  abstract fun validate(input: I): InputValidatorResult
}

@JsExport
interface InputValidatorResult

@JsExport
data object ValidInput : InputValidatorResult

@JsExport
data class InvalidInput(val reason: String = "") : InputValidatorResult
