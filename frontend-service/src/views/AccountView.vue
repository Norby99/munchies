<script setup lang="ts">
// Account settings: profile, password and account deletion, as three tabs.

import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { CircleAlertIcon, CircleCheckIcon, KeyRoundIcon, TrashIcon, TriangleAlertIcon, UserRoundIcon } from '@lucide/vue'

import ConfirmDialog from '@/components/ConfirmDialog.vue'
import PageHeader from '@/components/PageHeader.vue'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'
import { Field, FieldDescription, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { Spinner } from '@/components/ui/spinner'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { ApiError } from '@/api/client'
import { useSessionStore } from '@/stores/session'

const session = useSessionStore()
const router = useRouter()

const profileForm = reactive({
  username: session.user?.username ?? '',
  email: session.user?.email ?? '',
})
const profileError = ref<string | null>(null)
const profileSaved = ref(false)
const profileSaving = ref(false)

async function saveProfile(): Promise<void> {
  profileError.value = null
  profileSaved.value = false
  profileSaving.value = true
  try {
    await session.patchProfile({ ...profileForm })
    profileSaved.value = true
  } catch (err) {
    profileError.value = err instanceof ApiError ? err.message : 'Something went wrong.'
  } finally {
    profileSaving.value = false
  }
}

const passwordForm = reactive({ current: '', next: '' })
const passwordError = ref<string | null>(null)
const passwordSaved = ref(false)
const passwordSaving = ref(false)

async function changePassword(): Promise<void> {
  passwordError.value = null
  passwordSaved.value = false
  passwordSaving.value = true
  try {
    await session.changePassword(passwordForm.current, passwordForm.next)
    passwordForm.current = ''
    passwordForm.next = ''
    passwordSaved.value = true
  } catch (err) {
    passwordError.value = err instanceof ApiError ? err.message : 'Something went wrong.'
  } finally {
    passwordSaving.value = false
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
  <div class="mx-auto max-w-2xl">
    <PageHeader eyebrow="Account" :title="session.user?.username ?? 'Account'">
      <template #description>
        <span class="inline-flex items-center gap-2">
          {{ session.user?.email }}
          <Badge variant="secondary">{{ session.role === 'MANAGER' ? 'Manager' : 'Customer' }}</Badge>
        </span>
      </template>
    </PageHeader>

    <Tabs default-value="profile" class="gap-4">
      <TabsList class="w-full sm:w-fit">
        <TabsTrigger value="profile"><UserRoundIcon /> Profile</TabsTrigger>
        <TabsTrigger value="password"><KeyRoundIcon /> Password</TabsTrigger>
        <TabsTrigger value="danger"><TrashIcon /> Delete</TabsTrigger>
      </TabsList>

      <TabsContent value="profile">
        <Card>
          <form class="contents" @submit.prevent="saveProfile">
            <CardHeader>
              <CardTitle>Profile</CardTitle>
              <CardDescription>How you appear to restaurants and how we reach you.</CardDescription>
            </CardHeader>
            <CardContent>
              <FieldGroup>
                <Field>
                  <FieldLabel for="profile-username">Username</FieldLabel>
                  <Input id="profile-username" v-model="profileForm.username" class="h-10" type="text" autocomplete="username" />
                </Field>
                <Field>
                  <FieldLabel for="profile-email">Email</FieldLabel>
                  <Input id="profile-email" v-model="profileForm.email" class="h-10" type="email" autocomplete="email" />
                </Field>
                <Field>
                  <FieldLabel for="profile-role">Account type</FieldLabel>
                  <Input
                    id="profile-role"
                    class="h-10"
                    type="text"
                    :model-value="session.role === 'MANAGER' ? 'Manager' : 'Customer'"
                    readonly
                    disabled
                  />
                  <FieldDescription>The account type cannot be changed.</FieldDescription>
                </Field>
                <Alert v-if="profileError" variant="destructive" role="alert">
                  <CircleAlertIcon />
                  <AlertDescription>{{ profileError }}</AlertDescription>
                </Alert>
              </FieldGroup>
            </CardContent>
            <CardFooter class="justify-between gap-3">
              <p role="status" class="flex items-center gap-1.5 text-sm text-success">
                <template v-if="profileSaved"><CircleCheckIcon class="size-4" aria-hidden="true" /> Changes saved</template>
              </p>
              <Button type="submit" :disabled="profileSaving">
                <Spinner v-if="profileSaving" data-icon="inline-start" />
                Save changes
              </Button>
            </CardFooter>
          </form>
        </Card>
      </TabsContent>

      <TabsContent value="password">
        <Card>
          <form class="contents" @submit.prevent="changePassword">
            <CardHeader>
              <CardTitle>Change password</CardTitle>
              <CardDescription>Use at least 8 characters.</CardDescription>
            </CardHeader>
            <CardContent>
              <FieldGroup>
                <Alert>
                  <TriangleAlertIcon />
                  <AlertTitle>Double-check your current password</AlertTitle>
                  <AlertDescription>A wrong current password locks the account for one hour.</AlertDescription>
                </Alert>
                <Field>
                  <FieldLabel for="current-password">Current password</FieldLabel>
                  <Input id="current-password" v-model="passwordForm.current" class="h-10" type="password" autocomplete="current-password" />
                </Field>
                <Field>
                  <FieldLabel for="new-password">New password</FieldLabel>
                  <Input id="new-password" v-model="passwordForm.next" class="h-10" type="password" autocomplete="new-password" />
                </Field>
                <Alert v-if="session.lockoutMessage || passwordError" variant="destructive" role="alert">
                  <CircleAlertIcon />
                  <AlertDescription>{{ session.lockoutMessage ?? passwordError }}</AlertDescription>
                </Alert>
              </FieldGroup>
            </CardContent>
            <CardFooter class="justify-between gap-3">
              <p role="status" class="flex items-center gap-1.5 text-sm text-success">
                <template v-if="passwordSaved"><CircleCheckIcon class="size-4" aria-hidden="true" /> Password changed</template>
              </p>
              <Button type="submit" :disabled="passwordSaving || !passwordForm.current || !passwordForm.next">
                <Spinner v-if="passwordSaving" data-icon="inline-start" />
                Change password
              </Button>
            </CardFooter>
          </form>
        </Card>
      </TabsContent>

      <TabsContent value="danger">
        <Card class="ring-destructive/30">
          <CardHeader>
            <CardTitle>Delete account</CardTitle>
            <CardDescription>
              Your profile and credentials are removed immediately and you are signed out. This cannot be
              undone.
            </CardDescription>
          </CardHeader>
          <CardFooter class="justify-end">
            <Button variant="destructive" @click="deleteOpen = true"><TrashIcon data-icon="inline-start" /> Delete my account</Button>
          </CardFooter>
        </Card>
      </TabsContent>
    </Tabs>

    <ConfirmDialog
      :open="deleteOpen"
      title="Delete your account?"
      body="Your profile, credentials and session are removed permanently. Orders you placed stay with the restaurants."
      confirm-label="Delete permanently"
      cancel-label="Keep account"
      @cancel="deleteOpen = false"
      @confirm="confirmDelete"
    />
  </div>
</template>
