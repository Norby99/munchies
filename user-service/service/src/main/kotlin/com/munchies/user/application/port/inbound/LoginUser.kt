package com.munchies.user.application.port.inbound

interface LoginUser {
  /**
   * Authenticates a user based on the provided email and password.
   *
   * @param email the email address of the user attempting to log in.
   * @param password the password provided for authentication.
   * @return a [LoginResult] indicating the outcome of the login attempt, which may include
   *         success with user details or failure with an error message.
   */
  fun execute(email: String, username: String, password: String): LoginResult

  companion object {
    sealed interface LoginResult {
      data class Success(val userId: String) : LoginResult
      data object Failure : LoginResult
    }
  }
}
