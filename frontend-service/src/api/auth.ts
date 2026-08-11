// Register / login / verify — the three unauthenticated (or self-authenticating)
// user endpoints. Paths and payload shapes are taken from user-shared's Kotlin
// DTOs (UserServiceConfig, RegisterUserRequest, LoginUserRequest, VerifyEmailRequest),
// not just the API summary in ui_specification.md.

import { http, unwrap } from '@/api/client'
import type { Role, User } from '@/types'

const BASE = '/users/'

export interface LoginResult {
  id: string
  role: Role
}

export async function login(identifier: string, password: string): Promise<LoginResult> {
  const isEmail = identifier.includes('@')
  // LoginUserRequest has no default values, so both fields must be present —
  // the unused one goes across as an empty string.
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
  // UserDTO.id has no default in the Kotlin data class, so it must be present
  // in the JSON payload even though the server assigns the real id — the
  // gateway never reads the client-supplied value for a new registration.
  return unwrap(
    http.post(BASE + 'register/', {
      user: { id: '', username: input.username, email: input.email, role: input.role },
      hashedPassword: input.hashedPassword,
      saltValue: input.saltValue,
    }),
  )
}

export async function verifyEmail(otk: string): Promise<string> {
  // GET-with-body: the gateway route is a GET that still reads req.body.
  return unwrap(http.request({ method: 'get', url: BASE + 'verify-email/', data: { otk } }))
}
