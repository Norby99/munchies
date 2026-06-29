package com.munchies.user.infrastructure.adapter.inbound

import com.munchies.user.infrastructure.adapter.inbound.request.LoginUserRequest
import com.munchies.user.infrastructure.adapter.inbound.request.RegisterUserRequest
import com.munchies.user.infrastructure.adapter.inbound.request.UpdateUserPasswordRequest
import kotlin.js.JsExport

/**
 * Contract namespace for user-related inbound API operations.
 *
 * This sealed interface groups operation-specific contracts used by adapters
 * (for example, HTTP controllers/clients) while keeping each use case focused.
 */
@JsExport
object UserAPI {

  interface GetUserAPI<Response> {

    /**
     * Retrieves a user by its identifier.
     *
     * @param id user identifier.
     * @return the resolved user representation.
     */
    fun getUser(id: String): Response
  }

  /**
   * Contract for registering a new user.
   *
   * @param Response response payload type.
   */
  interface RegisterUserAPI<Response> {
    /**
     * Registers a user using the provided request payload.
     *
     * @param request registration input data.
     * @return operation response.
     */
    fun registerUser(request: RegisterUserRequest): Response
  }

  /**
   * Contract for authenticating a user.
   *
   * @param Response response payload type.
   */
  interface LoginUserAPI<Response> {
    /**
     * Authenticates a user with the provided credentials payload.
     *
     * @param request authentication input data.
     * @return operation response.
     */
    fun loginUser(request: LoginUserRequest): Response
  }

  /**
   * Contract for updating an existing user's password.
   *
   * @param Response response payload type.
   */
  interface UpdateUserPasswordAPI<Response> {
    /**
     * Updates user password information.
     *
     * @param request password update input data.
     * @return operation response.
     */
    fun updateUserPassword(request: UpdateUserPasswordRequest): Response
  }

  /**
   * Contract for updating an existing user's profile/info.
   *
   * @param Request request payload type.
   * @param Response response payload type.
   */
  interface UpdateUserInfoAPI<Request, Response> {
    /**
     * Updates user profile or account information.
     *
     * @param request user info update input data.
     * @return operation response.
     */
    fun updateUserInfo(request: Request): Response
  }

  interface DeleteUserAPI<Request, Response> {
    /**
     * Deletes a user account.
     *
     * @param request user deletion input data.
     * @return operation response.
     */
    fun deleteUser(request: Request): Response
  }

  interface EmailVerificationAPI<Response> {

    fun verifyEmail(id: String, otk: String): Response
  }
}
