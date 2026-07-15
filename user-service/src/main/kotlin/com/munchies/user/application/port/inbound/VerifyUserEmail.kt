package com.munchies.user.application.port.inbound

/**
 * Inbound port for confirming a user's email address using a one-time verification key.
 */
interface VerifyUserEmail {
  fun execute(id: String, otk: String): VerifyUserEmailResult

  companion object {
    sealed interface VerifyUserEmailResult {
      data object ConfirmedEmail : VerifyUserEmailResult
      data object InvalidRequest : VerifyUserEmailResult
    }
  }
}
