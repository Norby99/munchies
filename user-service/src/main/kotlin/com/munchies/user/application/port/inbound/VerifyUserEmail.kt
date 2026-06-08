package com.munchies.user.application.port.inbound

interface VerifyUserEmail {
  fun execute(id: String, otk: String): VerifyUserEmailResult

  companion object {
    sealed interface VerifyUserEmailResult {
      data object ConfirmedEmail : VerifyUserEmailResult
      data object InvalidRequest : VerifyUserEmailResult
    }
  }
}
