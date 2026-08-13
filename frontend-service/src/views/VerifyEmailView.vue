<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

import AuthLayout from '@/components/AuthLayout.vue'
import { ApiError } from '@/api/client'
import { homeFor } from '@/router'
import { useSessionStore } from '@/stores/session'

const session = useSessionStore()
const router = useRouter()

const otk = ref('')
const error = ref<string | null>(null)
const submitting = ref(false)

function skip(): void {
  router.push(homeFor(session.role))
}

async function onSubmit(): Promise<void> {
  error.value = null
  submitting.value = true
  try {
    await session.verify(otk.value)
    router.push(homeFor(session.role))
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : 'Something went wrong.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <AuthLayout kicker="Step 2 of 2" heading="Verify email">
    <p class="text-muted" style="font-size: 14px">
      We sent a one-time key to
      <strong style="color: var(--color-text)">{{ session.user?.email }}</strong>. Registration
      already signed you in, so this can be skipped and resumed later.
    </p>
    <div class="hr"></div>

    <form @submit.prevent="onSubmit">
      <div class="field" style="margin-bottom: 16px">
        <label for="otk">One-time key</label>
        <input
          id="otk"
          class="verify-view__otk-input input"
          type="text"
          inputmode="numeric"
          autocomplete="one-time-code"
          placeholder="000000"
          v-model="otk"
        />
      </div>

      <p v-if="error" role="alert" style="font-size: 13px; color: var(--color-accent-700); margin: 0 0 12px">
        {{ error }}
      </p>

      <button class="btn btn-primary btn-block" type="submit" :disabled="submitting">Verify</button>
    </form>

    <div style="display: flex; flex-wrap: wrap; gap: 10px; align-items: center; margin-top: 18px; font-size: 13px">
      <a href="#" @click.prevent="skip">Skip for now</a>
      <span style="opacity: 0.35">·</span>
      <span
        class="tag tag-outline"
        style="flex: none; white-space: nowrap"
        title="No resend-OTK endpoint is routed through the gateway"
      >
        Resend key — needs backend
      </span>
    </div>
    <p class="text-muted" style="font-size: 12px; margin-top: 16px">
      Verification status is never returned by the API, so no screen can show a "verified" badge.
    </p>
  </AuthLayout>
</template>

<style scoped>
.verify-view__otk-input {
  font-size: 22px;
  letter-spacing: 0.4em;
  min-height: 52px;
}
</style>
