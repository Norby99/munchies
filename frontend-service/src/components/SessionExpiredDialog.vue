<script setup lang="ts">
// Raised by the axios response interceptor on a 401, not by any one view —
// the cookie lasts one hour even though the JWT is valid for seven days.

import { useRouter } from 'vue-router'

import { useSessionStore } from '@/stores/session'

const session = useSessionStore()
const router = useRouter()

function signInAgain(): void {
  session.acknowledgeExpired()
  router.push({ name: 'login' })
}
</script>

<template>
  <div v-if="session.expired" class="dialog-backdrop" role="dialog" aria-modal="true" aria-labelledby="exp-title">
    <div class="dialog">
      <span class="tag tag-outline" style="align-self: flex-start">code: 401</span>
      <h3 class="dialog-title" id="exp-title">Session expired</h3>
      <p class="dialog-body" style="margin: 0">
        The cookie lasts one hour even though the JWT is valid for seven days. The interceptor
        caught the 401 and kept your draft in the store — signing back in returns you here.
      </p>
      <div class="dialog-actions">
        <button class="btn btn-primary" type="button" style="min-height: 44px" @click="signInAgain">
          Sign in again
        </button>
      </div>
    </div>
  </div>
</template>
