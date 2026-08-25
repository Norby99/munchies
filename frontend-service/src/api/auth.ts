// Register / login / verify — the three unauthenticated (or self-authenticating)
// user endpoints. Paths and payload shapes are taken from user-shared's Kotlin
// DTOs (UserServiceConfig, RegisterUserRequest, LoginUserRequest, VerifyEmailRequest)

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

export async function verifyEmail(otk: string): Promise<string> {
  return unwrap(http.request({ method: 'get', url: BASE + 'verify-email/', data: { otk } }))
}
