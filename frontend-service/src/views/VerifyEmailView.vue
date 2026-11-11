<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { CircleAlertIcon, MailCheckIcon } from '@lucide/vue'

import AuthLayout from '@/components/AuthLayout.vue'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Field, FieldDescription, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { Spinner } from '@/components/ui/spinner'
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
    await session.verify(otk.value.trim())
    router.push(homeFor(session.role))
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : 'Something went wrong. Please try again.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <AuthLayout heading="Check your inbox">
    <div class="mb-6 flex items-start gap-3 rounded-lg bg-muted p-3 text-sm">
      <MailCheckIcon class="mt-0.5 size-5 shrink-0 text-primary" aria-hidden="true" />
      <p>
        We sent a verification key to
        <strong class="font-semibold break-all">{{ session.user?.email }}</strong>. You are already signed in,
        so you can also verify later.
      </p>
    </div>

    <form @submit.prevent="onSubmit">
      <FieldGroup>
        <Field>
          <FieldLabel for="otk">Verification key</FieldLabel>
          <!-- The key is a 64-character hex hash (user-service), meant to be pasted, not typed. -->
          <Input
            id="otk"
            v-model="otk"
            class="h-10 font-mono text-sm"
            type="text"
            autocomplete="one-time-code"
            autocapitalize="off"
            spellcheck="false"
            aria-describedby="otk-hint"
            required
          />
          <FieldDescription id="otk-hint">Paste the key from the email: a long code of letters and digits.</FieldDescription>
        </Field>

        <Alert v-if="error" variant="destructive" role="alert">
          <CircleAlertIcon />
          <AlertDescription>{{ error }}</AlertDescription>
        </Alert>

        <div class="flex flex-col gap-2">
          <Button type="submit" size="lg" class="h-10 w-full" :disabled="submitting">
            <Spinner v-if="submitting" data-icon="inline-start" />
            Verify email
          </Button>
          <Button type="button" variant="ghost" size="lg" class="h-10 w-full" @click="skip">Skip for now</Button>
        </div>
      </FieldGroup>
    </form>
  </AuthLayout>
</template>
