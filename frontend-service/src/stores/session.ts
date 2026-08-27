// Session state: the authenticated user, their role, and the auth actions.
// The session cookie itself is httpOnly — this store is just the client's
// belief about who is signed in, hydrated from GET /users/ on demand.

import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

import * as authApi from '@/api/auth'
import { ApiError, onUnauthorized } from '@/api/client'
import * as usersApi from '@/api/users'
import { hashPassword, randomSalt } from '@/utils/hash'
import type { LoadStatus, Role, User } from '@/types'

export const useSessionStore = defineStore('session', () => {
  const user = ref<User | null>(null)
  const role = ref<Role | null>(null)
  const returnTo = ref<string | null>(null)
  const status = ref<LoadStatus>('ready')
  const lockoutMessage = ref<string | null>(null)
  // Raised by the axios response interceptor on a 401, independent of
  // whatever view is on screen — the cookie lasts 1h even though the JWT is
  // valid 7 days, so this can happen mid-session (see SessionExpiredDialog).
  const expired = ref(false)

  const isAuthenticated = computed(() => user.value !== null)

  function clear(): void {
    user.value = null
    role.value = null
    status.value = 'ready'
  }

  async function hydrate(): Promise<void> {
    status.value = 'loading'
    try {
      const profile = await usersApi.getProfile()
      user.value = profile
      role.value = profile.role
      status.value = 'ready'
    } catch {
      clear()
    }
  }

  async function login(identifier: string, password: string): Promise<void> {
    lockoutMessage.value = null
    try {
      const result = await authApi.login(identifier, password)
      role.value = result.role
      // Login only returns { id, role } — fetch the full profile to hydrate
      // username/email for the rest of the app.
      await hydrate()
    } catch (err) {
      if (err instanceof ApiError && /locked/i.test(err.message)) {
        lockoutMessage.value = err.message
      }
      throw err
    }
  }

  async function register(username: string, email: string, password: string, accountRole: Role): Promise<void> {
    const saltValue = randomSalt()
    const hashedPassword = await hashPassword(password, saltValue)
    const created = await authApi.register({ username, email, role: accountRole, hashedPassword, saltValue })
    user.value = created
    role.value = created.role
  }

  async function verify(otk: string): Promise<void> {
    await authApi.verifyEmail(otk)
  }

  // No logout endpoint exists — this clears client state and the session
  // belief only, it cannot revoke the cookie server-side.
  function logout(): void {
    clear()
  }

  async function patchProfile(input: { username: string; email: string }): Promise<void> {
    if (!role.value || !user.value) throw new Error('Not authenticated')
    // isEmailVerified must be forwarded from the current known value — UpdateUserInfoRequest
    // takes a whole UserDTO, and the field defaults to false server-side if omitted.
    await usersApi.updateProfile({
      ...input,
      role: role.value,
      isEmailVerified: user.value.isEmailVerified,
    })
    user.value = { ...user.value, ...input }
  }

  async function changePassword(currentPassword: string, newPassword: string): Promise<void> {
    lockoutMessage.value = null
    if (!user.value) throw new Error('Not authenticated')
    try {
      await usersApi.changePassword({
        username: user.value.username,
        email: user.value.email,
        currentPassword,
        newPassword,
      })
    } catch (err) {
      if (err instanceof ApiError && /locked/i.test(err.message)) {
        lockoutMessage.value = err.message
      }
      throw err
    }
  }

  async function deleteAccount(): Promise<void> {
    await usersApi.deleteAccount()
    clear()
  }

  onUnauthorized(() => {
    clear()
    expired.value = true
  })

  function acknowledgeExpired(): void {
    expired.value = false
  }

  return {
    user,
    role,
    returnTo,
    status,
    lockoutMessage,
    expired,
    isAuthenticated,
    hydrate,
    login,
    register,
    verify,
    logout,
    patchProfile,
    changePassword,
    deleteAccount,
    acknowledgeExpired,
  }
})
