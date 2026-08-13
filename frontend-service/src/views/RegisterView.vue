<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import AuthLayout from '@/components/AuthLayout.vue'
import { ApiError } from '@/api/client'
import { useSessionStore } from '@/stores/session'
import type { Role } from '@/types'

const session = useSessionStore()
const router = useRouter()

const form = reactive<{ username: string; email: string; password: string; confirmPassword: string; role: Role }>({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  role: 'CUSTOMER',
})
const fieldErrors = reactive<Record<string, string>>({})
const formError = ref<string | null>(null)
const submitting = ref(false)

function validate(): boolean {
  Object.keys(fieldErrors).forEach((key) => delete fieldErrors[key])

  if (!form.username.trim()) fieldErrors.username = 'Username is required.'
  if (!form.email.trim()) fieldErrors.email = 'Email is required.'
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) fieldErrors.email = 'Enter a valid email address.'
  if (form.password.length < 8) fieldErrors.password = 'Password must be at least 8 characters.'
  if (form.confirmPassword !== form.password) fieldErrors.confirmPassword = 'Passwords do not match.'

  return Object.keys(fieldErrors).length === 0
}

async function onSubmit(): Promise<void> {
  formError.value = null
  if (!validate()) return

  submitting.value = true
  try {
    await session.register(form.username.trim(), form.email.trim(), form.password, form.role)
    router.push({ name: 'verify' })
  } catch (err) {
    formError.value = err instanceof ApiError ? err.message : 'Something went wrong.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <AuthLayout kicker="Create account" heading="Register">
    <div class="hr"></div>

    <form novalidate @submit.prevent="onSubmit">
      <div class="field" style="margin-bottom: 14px">
        <label for="reg-user">Username</label>
        <input id="reg-user" class="input" type="text" placeholder="chiara" v-model="form.username" />
        <p v-if="fieldErrors.username" class="register-view__field-error">{{ fieldErrors.username }}</p>
      </div>
      <div class="field" style="margin-bottom: 14px">
        <label for="reg-email">Email</label>
        <input id="reg-email" class="input" type="email" placeholder="chiara@example.com" v-model="form.email" />
        <p v-if="fieldErrors.email" class="register-view__field-error">{{ fieldErrors.email }}</p>
      </div>
      <div class="field" style="margin-bottom: 14px">
        <label for="reg-pw">Password</label>
        <input id="reg-pw" class="input" type="password" autocomplete="new-password" v-model="form.password" />
        <p v-if="fieldErrors.password" class="register-view__field-error">{{ fieldErrors.password }}</p>
      </div>
      <div class="field" style="margin-bottom: 16px">
        <label for="reg-pw2">Confirm password</label>
        <input
          id="reg-pw2"
          class="input"
          type="password"
          autocomplete="new-password"
          v-model="form.confirmPassword"
        />
        <p v-if="fieldErrors.confirmPassword" class="register-view__field-error">
          {{ fieldErrors.confirmPassword }}
        </p>
      </div>

      <div class="field" style="margin-bottom: 16px">
        <span id="reg-account-type-label" class="register-view__group-label">Account type</span>
        <div class="seg" role="group" aria-labelledby="reg-account-type-label" style="margin-top: 5px">
          <label class="seg-opt">
            <input type="radio" name="reg-role" :checked="form.role === 'CUSTOMER'" @change="form.role = 'CUSTOMER'" />
            <span>Customer</span>
          </label>
          <label class="seg-opt">
            <input type="radio" name="reg-role" :checked="form.role === 'MANAGER'" @change="form.role = 'MANAGER'" />
            <span>Manager</span>
          </label>
        </div>
      </div>

      <div class="register-view__explainer">
        <h6 style="margin: 0 0 6px">Sent to the API</h6>
        <p class="text-muted" style="font-size: 12px; margin: 0">
          Role travels as <strong>{{ form.role }}</strong> in the register payload. The password
          leaves as <code>SHA-256(password + salt)</code> with a fresh 16-byte hex salt.
        </p>
      </div>

      <p v-if="formError" role="alert" style="font-size: 13px; color: var(--color-accent-700); margin: 0 0 12px">
        {{ formError }}
      </p>

      <button class="btn btn-primary btn-block" type="submit" :disabled="submitting">Create account</button>
    </form>

    <p style="font-size: 13px; margin-top: 18px">
      <router-link :to="{ name: 'login' }">Back to sign in</router-link>
    </p>
  </AuthLayout>
</template>

<style scoped>
.register-view__field-error {
  font-size: 12px;
  color: var(--color-accent-700);
  margin: 4px 0 0;
}

.register-view__group-label {
  display: block;
  font-size: 12px;
  color: color-mix(in srgb, var(--color-text) 70%, transparent);
}

.register-view__explainer {
  border: 1px solid var(--color-divider);
  padding: 12px;
  margin-bottom: 16px;
}
</style>
