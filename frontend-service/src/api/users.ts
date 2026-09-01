// Authenticated user-profile endpoints. The id is always injected server-side
// from the session cookie (UpdateUserInfoRequest.addId / UpdateUserPasswordRequest.addId
// in user-shared) — the client-sent id is never trusted, but the field is
// still required on the wire because the Kotlin DTOs give it no default.

import { http, unwrap } from '@/api/client'
import type { Role, User } from '@/types'

const BASE = '/users/'

export async function getProfile(): Promise<User> {
  return unwrap(http.get(BASE))
}

export async function updateProfile(
  user: { username: string; email: string; role: Role; isEmailVerified: boolean },
): Promise<string> {
  return unwrap(http.patch(BASE + 'update-info/', { user: { id: '', ...user } }))
}

export interface ChangePasswordInput {
  username: string
  email: string
  currentPassword: string
  newPassword: string
}

export async function changePassword(input: ChangePasswordInput): Promise<string> {
  // The `oldHashedPassword` field name is misleading — per ui_specification.md
  // §4.2 the current password travels in plaintext, same as newPassword; the
  // request has no salt field to hash against.
  return unwrap(
    http.patch(BASE + 'update-password/', {
      id: '',
      username: input.username,
      email: input.email,
      oldHashedPassword: input.currentPassword,
      newPassword: input.newPassword,
    }),
  )
}

export async function deleteAccount(): Promise<User> {
  return unwrap(http.delete(BASE))
}
