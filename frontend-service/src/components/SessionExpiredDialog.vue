<script setup lang="ts">
// Raised by the axios response interceptor on a 401, not by any one view: the session cookie lasts
// one hour. The cart and the current route are kept, so signing in again returns the user here.

import { useRouter } from 'vue-router'
import { ClockAlertIcon } from '@lucide/vue'

import {
  AlertDialog,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogMedia,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'
import { Button } from '@/components/ui/button'
import { useSessionStore } from '@/stores/session'

const session = useSessionStore()
const router = useRouter()

function signInAgain(): void {
  session.acknowledgeExpired()
  router.push({ name: 'login' })
}
</script>

<template>
  <AlertDialog :open="session.expired">
    <AlertDialogContent>
      <AlertDialogHeader>
        <AlertDialogMedia>
          <ClockAlertIcon />
        </AlertDialogMedia>
        <AlertDialogTitle>Your session has expired</AlertDialogTitle>
        <AlertDialogDescription>
          For your security you are signed out after an hour. Sign in again to pick up where you left
          off: your cart is still here.
        </AlertDialogDescription>
      </AlertDialogHeader>
      <AlertDialogFooter>
        <Button @click="signInAgain">Sign in again</Button>
      </AlertDialogFooter>
    </AlertDialogContent>
  </AlertDialog>
</template>
