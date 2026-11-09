// Session-related user endpoints: register, login, verify email, logout. Paths and payload
// shapes are taken from the Kotlin DTOs in user-shared (UserServiceConfig, RegisterUserRequest,
// LoginUserRequest, VerifyEmailRequest) and gateway-shared (GatewayServiceConfig,
// LogoutUserRequest).

import { http, unwrap } from '@/api/client'
import type { Role, User } from '@/types'

const BASE = '/users/'

export interface LoginResult {
  id: string
  role: Role
}

export async function login(identifier: string, password: string): Promise<LoginResult> {
  const isEmail = identifier.includes('@')
  return unwrap(
    http.post(BASE + 'login/', {
      email: isEmail ? identifier : '',
      username: isEmail ? '' : identifier,
      password,
    }),
  )
}

export interface RegisterInput {
  username: string
  email: string
  role: Role
  hashedPassword: string
  saltValue: string
}

export async function register(input: RegisterInput): Promise<User> {
  return unwrap(
    http.post(BASE + 'register/', {
      username: input.username,
      email: input.email,
      role: input.role,
      hashedPassword: input.hashedPassword,
      saltValue: input.saltValue,
    }),
  )
}

// POST and authenticated (JsEmailVerificationAPI): the gateway injects the user id from the
// session cookie, but VerifyEmailRequest declares `id` without a default, so it must be present.
export async function verifyEmail(otk: string): Promise<string> {
  return unwrap(http.post(BASE + 'verify-email/', { id: '', otk }))
}

// Handled by the gateway itself (JsLogoutUserAPI): it clears the httpOnly session cookie, which
// the client cannot do on its own. `userId` is injected server-side from that same cookie.
export async function logout(): Promise<string> {
  return unwrap(http.post(BASE + 'logout/', { userId: '' }))
}
