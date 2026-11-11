<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { CircleAlertIcon, ShoppingBagIcon, StoreIcon } from '@lucide/vue'

import AuthLayout from '@/components/AuthLayout.vue'
import ChoiceCard from '@/components/ChoiceCard.vue'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel, FieldLegend, FieldSet } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { RadioGroup } from '@/components/ui/radio-group'
import { Spinner } from '@/components/ui/spinner'
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

  if (!form.username.trim()) fieldErrors.username = 'Choose a username.'
  if (!form.email.trim()) fieldErrors.email = 'Enter your email address.'
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) fieldErrors.email = 'Enter a valid email address.'
  if (form.password.length < 8) fieldErrors.password = 'Use at least 8 characters.'
  if (form.confirmPassword !== form.password) fieldErrors.confirmPassword = 'The passwords do not match.'

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
    formError.value = err instanceof ApiError ? err.message : 'Something went wrong. Please try again.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <AuthLayout heading="Create your account" description="It takes less than a minute.">
    <form novalidate @submit.prevent="onSubmit">
      <FieldGroup>
        <FieldSet>
          <FieldLegend variant="label">I want to</FieldLegend>
          <RadioGroup v-model="form.role" class="grid gap-3 sm:grid-cols-2">
            <ChoiceCard id="reg-role-customer" value="CUSTOMER" title="Order food" description="Customer" :icon="ShoppingBagIcon" />
            <ChoiceCard id="reg-role-manager" value="MANAGER" title="Run a restaurant" description="Manager" :icon="StoreIcon" />
          </RadioGroup>
        </FieldSet>

        <Field :data-invalid="!!fieldErrors.username || undefined">
          <FieldLabel for="reg-user">Username</FieldLabel>
          <Input
            id="reg-user"
            v-model="form.username"
            class="h-10"
            type="text"
            autocomplete="username"
            placeholder="chiara"
            :aria-invalid="!!fieldErrors.username || undefined"
          />
          <FieldError v-if="fieldErrors.username">{{ fieldErrors.username }}</FieldError>
        </Field>
        <Field :data-invalid="!!fieldErrors.email || undefined">
          <FieldLabel for="reg-email">Email</FieldLabel>
          <Input
            id="reg-email"
            v-model="form.email"
            class="h-10"
            type="email"
            autocomplete="email"
            placeholder="chiara@example.com"
            :aria-invalid="!!fieldErrors.email || undefined"
          />
          <FieldError v-if="fieldErrors.email">{{ fieldErrors.email }}</FieldError>
        </Field>
        <div class="grid gap-4 sm:grid-cols-2">
          <Field :data-invalid="!!fieldErrors.password || undefined">
            <FieldLabel for="reg-pw">Password</FieldLabel>
            <Input
              id="reg-pw"
              v-model="form.password"
              class="h-10"
              type="password"
              autocomplete="new-password"
              :aria-invalid="!!fieldErrors.password || undefined"
              aria-describedby="reg-pw-hint"
            />
            <FieldError v-if="fieldErrors.password">{{ fieldErrors.password }}</FieldError>
            <FieldDescription v-else id="reg-pw-hint">At least 8 characters.</FieldDescription>
          </Field>
          <Field :data-invalid="!!fieldErrors.confirmPassword || undefined">
            <FieldLabel for="reg-pw2">Confirm password</FieldLabel>
            <Input
              id="reg-pw2"
              v-model="form.confirmPassword"
              class="h-10"
              type="password"
              autocomplete="new-password"
              :aria-invalid="!!fieldErrors.confirmPassword || undefined"
            />
            <FieldError v-if="fieldErrors.confirmPassword">{{ fieldErrors.confirmPassword }}</FieldError>
          </Field>
        </div>

        <Alert v-if="formError" variant="destructive" role="alert">
          <CircleAlertIcon />
          <AlertDescription>{{ formError }}</AlertDescription>
        </Alert>

        <Button type="submit" size="lg" class="h-10 w-full" :disabled="submitting">
          <Spinner v-if="submitting" data-icon="inline-start" />
          Create account
        </Button>
      </FieldGroup>
    </form>

    <template #footer>
      Already have an account?
      <RouterLink :to="{ name: 'login' }" class="font-semibold text-primary underline-offset-4 hover:underline">
        Sign in
      </RouterLink>
    </template>
  </AuthLayout>
</template>
