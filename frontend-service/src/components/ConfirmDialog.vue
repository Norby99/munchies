<script setup lang="ts">
// Generic confirmation, reused for every destructive action (delete restaurant, menu, category,
// item, account; cancel order). Built on the shadcn-vue AlertDialog (Reka UI), which traps focus,
// restores it to the trigger on close and closes on Escape.

import { TriangleAlertIcon } from '@lucide/vue'

import {
  AlertDialog,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogMedia,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'
import { Button } from '@/components/ui/button'

withDefaults(
  defineProps<{
    open: boolean
    title: string
    body: string
    confirmLabel?: string
    cancelLabel?: string
    pending?: boolean
  }>(),
  { confirmLabel: 'Confirm', cancelLabel: 'Cancel', pending: false },
)

const emit = defineEmits<{ confirm: []; cancel: [] }>()

function onOpenChange(open: boolean): void {
  if (!open) emit('cancel')
}
</script>

<template>
  <AlertDialog :open="open" @update:open="onOpenChange">
    <AlertDialogContent>
      <AlertDialogHeader>
        <AlertDialogMedia class="bg-destructive/10 text-destructive">
          <TriangleAlertIcon />
        </AlertDialogMedia>
        <AlertDialogTitle>{{ title }}</AlertDialogTitle>
        <AlertDialogDescription>{{ body }}</AlertDialogDescription>
      </AlertDialogHeader>
      <AlertDialogFooter>
        <AlertDialogCancel>{{ cancelLabel }}</AlertDialogCancel>
        <!-- A plain button, not AlertDialogAction: the dialog closes only once the parent has
             handled the confirmation (and possibly shown an error in the body). -->
        <Button variant="destructive" :disabled="pending" @click="emit('confirm')">{{ confirmLabel }}</Button>
      </AlertDialogFooter>
    </AlertDialogContent>
  </AlertDialog>
</template>
