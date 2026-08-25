<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

import AuthLayout from '@/components/AuthLayout.vue'
import { ApiError } from '@/api/client'
import { homeFor } from '@/router'
import { useSessionStore } from '@/stores/session'

const session = useSessionStore()
const router = useRouter()

const identifier = ref('')
const password = ref('')
const error = ref<string | null>(null)
const submitting = ref(false)

async function onSubmit(): Promise<void> {
  error.value = null
  submitting.value = true
  try {
    await session.login(identifier.value, password.value)
    const destination = session.returnTo ?? homeFor(session.role)
    session.returnTo = null
    router.push(destination)
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : 'Something went wrong.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <AuthLayout kicker="Sign in" heading="Munchies">
    <p class="text-muted" style="font-size: 14px; max-width: 300px">
      One session cookie, one hour. The client never touches the token.
    </p>
    <div class="hr"></div>

    <form @submit.prevent="onSubmit">
      <div class="field" style="margin-bottom: 14px">
        <label for="login-id">Email or username</label>
        <input
          id="login-id"
          class="input"
          type="text"
          autocomplete="username"
          placeholder="chiara@example.com"
          v-model="identifier"
          required
        />
      </div>
      <div class="field" style="margin-bottom: 16px">
        <label for="login-pw">Password</label>
        <input
          id="login-pw"
          class="input"
          type="password"
          autocomplete="current-password"
          v-model="password"
          required
        />
        <p class="text-muted" style="font-size: 11px; margin: 6px 0 0">
          Sent as plaintext over HTTPS — login does not hash client-side.
        </p>
      </div>

      <div v-if="session.lockoutMessage" role="alert" class="login-view__lockout">
        <strong style="font-size: 13px; display: block">Account locked for 1 hour</strong>
        <span style="font-size: 13px; color: var(--color-accent-800)">{{ session.lockoutMessage }}</span>
      </div>

      <p v-else-if="error" role="alert" style="font-size: 13px; color: var(--color-accent-700); margin: 0 0 12px">
        {{ error }}
      </p>

      <button class="btn btn-primary btn-block" type="submit" :disabled="submitting">Sign in</button>
    </form>

    <div style="display: flex; flex-wrap: wrap; gap: 8px; align-items: center; margin-top: 18px; font-size: 13px">
      <span class="text-muted">No account?</span>
      <router-link :to="{ name: 'register' }">Create one</router-link>
      <span style="opacity: 0.35">·</span>
      <span
        class="tag tag-outline"
        style="flex: none; white-space: nowrap"
        title="No password-reset endpoint exists"
      >
        Forgot password — needs backend
      </span>
    </div>
  </AuthLayout>
</template>

<style scoped>
.login-view__lockout {
  border-left: 2px solid var(--color-accent);
  padding: 10px 12px;
  background: var(--color-accent-100);
  margin-bottom: 14px;
}
</style>
