<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { CircleAlertIcon, LockIcon } from '@lucide/vue'

import AuthLayout from '@/components/AuthLayout.vue'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Field, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { Spinner } from '@/components/ui/spinner'
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
    error.value = err instanceof ApiError ? err.message : 'Something went wrong. Please try again.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <AuthLayout heading="Welcome back" description="Sign in to order or to manage your restaurant.">
    <form @submit.prevent="onSubmit">
      <FieldGroup>
        <Field>
          <FieldLabel for="login-id">Email or username</FieldLabel>
          <Input
            id="login-id"
            v-model="identifier"
            class="h-10"
            type="text"
            autocomplete="username"
            placeholder="chiara@example.com"
            required
          />
        </Field>
        <Field>
          <FieldLabel for="login-pw">Password</FieldLabel>
          <Input id="login-pw" v-model="password" class="h-10" type="password" autocomplete="current-password" required />
        </Field>

        <Alert v-if="session.lockoutMessage" variant="destructive" role="alert">
          <LockIcon />
          <AlertTitle>Account locked for one hour</AlertTitle>
          <AlertDescription>{{ session.lockoutMessage }}</AlertDescription>
        </Alert>
        <Alert v-else-if="error" variant="destructive" role="alert">
          <CircleAlertIcon />
          <AlertTitle>Could not sign in</AlertTitle>
          <AlertDescription>{{ error }}</AlertDescription>
        </Alert>

        <Button type="submit" size="lg" class="h-10 w-full" :disabled="submitting">
          <Spinner v-if="submitting" data-icon="inline-start" />
          Sign in
        </Button>
      </FieldGroup>
    </form>

    <template #footer>
      New to Munchies?
      <RouterLink :to="{ name: 'register' }" class="font-semibold text-primary underline-offset-4 hover:underline">
        Create an account
      </RouterLink>
    </template>
  </AuthLayout>
</template>
