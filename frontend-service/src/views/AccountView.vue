<script setup lang="ts">
// Account settings (README screen 7): profile, password, delete — three tabs
// in a single `.seg` control, matching the design's fixed max-width forms.

import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import ConfirmDialog from '@/components/ConfirmDialog.vue'
import { ApiError } from '@/api/client'
import { useSessionStore } from '@/stores/session'

const session = useSessionStore()
const router = useRouter()

type Tab = 'profile' | 'password' | 'danger'
const activeTab = ref<Tab>('profile')
const tabs: { id: Tab; label: string }[] = [
  { id: 'profile', label: 'Profile' },
  { id: 'password', label: 'Password' },
  { id: 'danger', label: 'Delete account' },
]

const profileForm = reactive({
  username: session.user?.username ?? '',
  email: session.user?.email ?? '',
})
const profileError = ref<string | null>(null)
const profileSaved = ref(false)

async function saveProfile(): Promise<void> {
  profileError.value = null
  profileSaved.value = false
  try {
    await session.patchProfile({ ...profileForm })
    profileSaved.value = true
  } catch (err) {
    profileError.value = err instanceof ApiError ? err.message : 'Something went wrong.'
  }
}

const passwordForm = reactive({ current: '', next: '' })
const passwordError = ref<string | null>(null)
const passwordSaved = ref(false)

async function changePassword(): Promise<void> {
  passwordError.value = null
  passwordSaved.value = false
  try {
    await session.changePassword(passwordForm.current, passwordForm.next)
    passwordForm.current = ''
    passwordForm.next = ''
    passwordSaved.value = true
  } catch (err) {
    passwordError.value = err instanceof ApiError ? err.message : 'Something went wrong.'
  }
}

const deleteOpen = ref(false)

async function confirmDelete(): Promise<void> {
  await session.deleteAccount()
  deleteOpen.value = false
  router.push({ name: 'login' })
}
</script>

<template>
  <div style="max-width: 640px">
    <h6 style="color: var(--color-accent)">Account</h6>
    <h2 style="margin: 0 0 16px">{{ session.user?.username }}</h2>

    <div class="seg" style="flex-wrap: wrap">
      <label v-for="tab in tabs" :key="tab.id" class="seg-opt" style="min-height: 44px">
        <input type="radio" name="account-tab" :checked="activeTab === tab.id" @change="activeTab = tab.id" />
        <span>{{ tab.label }}</span>
      </label>
    </div>
    <div class="hr"></div>

    <div v-if="activeTab === 'profile'">
      <div class="field" style="margin-bottom: 14px; max-width: 360px">
        <label for="profile-username">Username</label>
        <input id="profile-username" class="input" type="text" v-model="profileForm.username" />
      </div>
      <div class="field" style="margin-bottom: 14px; max-width: 360px">
        <label for="profile-email">Email</label>
        <input id="profile-email" class="input" type="email" v-model="profileForm.email" />
      </div>
      <div class="field" style="margin-bottom: 16px; max-width: 360px">
        <label for="profile-role">Role</label>
        <input id="profile-role" class="input" type="text" :value="session.role" readonly disabled />
        <p class="text-muted" style="font-size: 11px; margin: 4px 0 0">
          The PATCH payload accepts a role, so the control stays locked to avoid a self-service
          privilege change.
        </p>
      </div>
      <p v-if="profileError" role="alert" style="font-size: 13px; color: var(--color-accent-700)">
        {{ profileError }}
      </p>
      <p v-else-if="profileSaved" style="font-size: 13px; color: var(--color-accent-700)">Saved.</p>
      <button class="btn btn-primary" type="button" style="min-height: 44px" @click="saveProfile">
        Save changes
      </button>
      <p class="text-muted" style="font-size: 12px; margin-top: 16px">
        Avatar, address and preferences do not exist in the user domain, so this form has no room
        to grow without a backend change.
      </p>
    </div>

    <div v-else-if="activeTab === 'password'">
      <div role="alert" class="account-view__warning">
        <strong style="font-size: 13px; display: block">One wrong attempt locks the account for an hour</strong>
        <span style="font-size: 13px; color: var(--color-accent-800)">
          There is no multi-strike grace. Check the current password before submitting.
        </span>
      </div>
      <div class="field" style="margin-bottom: 14px; max-width: 360px">
        <label for="current-password">Current password</label>
        <input
          id="current-password"
          class="input"
          type="password"
          autocomplete="current-password"
          v-model="passwordForm.current"
        />
      </div>
      <div class="field" style="margin-bottom: 16px; max-width: 360px">
        <label for="new-password">New password</label>
        <input
          id="new-password"
          class="input"
          type="password"
          autocomplete="new-password"
          v-model="passwordForm.next"
        />
      </div>
      <p v-if="session.lockoutMessage" role="alert" style="font-size: 13px; color: var(--color-accent-700)">
        {{ session.lockoutMessage }}
      </p>
      <p v-else-if="passwordError" role="alert" style="font-size: 13px; color: var(--color-accent-700)">
        {{ passwordError }}
      </p>
      <p v-else-if="passwordSaved" style="font-size: 13px; color: var(--color-accent-700)">
        Password changed.
      </p>
      <button class="btn btn-primary" type="button" style="min-height: 44px" @click="changePassword">
        Change password
      </button>
    </div>

    <div v-else>
      <h4 style="margin: 0 0 6px">Delete this account</h4>
      <p class="text-muted" style="font-size: 14px; max-width: 46ch; text-wrap: pretty">
        The account and its credentials are removed immediately. The backend takes no confirmation
        token, so this dialog is the only guard that exists.
      </p>
      <div class="hr"></div>
      <button class="btn btn-primary" type="button" style="min-height: 44px" @click="deleteOpen = true">
        Delete account
      </button>
    </div>

    <ConfirmDialog
      :open="deleteOpen"
      title="Delete your account?"
      body="Your profile, credentials and session are removed. Placed orders stay in the order service under an id nothing can resolve to you."
      confirm-label="Delete permanently"
      cancel-label="Keep account"
      @cancel="deleteOpen = false"
      @confirm="confirmDelete"
    />
  </div>
</template>

<style scoped>
.account-view__warning {
  border-left: 2px solid var(--color-accent);
  background: var(--color-accent-100);
  padding: 12px 14px;
  margin-bottom: 18px;
  max-width: 420px;
}
</style>
