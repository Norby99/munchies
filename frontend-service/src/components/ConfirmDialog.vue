<script setup lang="ts">
// Generic confirm dialog, reused for every destructive confirmation (delete
// restaurant, delete menu/category/item, delete account). Traps focus inside
// itself while open, restores it to the trigger on close, and closes on Escape.

import { nextTick, ref, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    open: boolean
    title: string
    body: string
    confirmLabel?: string
    cancelLabel?: string
  }>(),
  { confirmLabel: 'Confirm', cancelLabel: 'Cancel' },
)

const emit = defineEmits<{ confirm: []; cancel: [] }>()

const dialogRef = ref<HTMLElement | null>(null)
let previouslyFocused: HTMLElement | null = null

function focusableElements(): HTMLElement[] {
  if (!dialogRef.value) return []
  return Array.from(
    dialogRef.value.querySelectorAll<HTMLElement>(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])',
    ),
  )
}

function onKeydown(event: KeyboardEvent): void {
  if (event.key === 'Escape') {
    emit('cancel')
    return
  }
  if (event.key !== 'Tab') return

  const focusable = focusableElements()
  if (focusable.length === 0) return

  const first = focusable[0]
  const last = focusable[focusable.length - 1]

  if (event.shiftKey && document.activeElement === first) {
    event.preventDefault()
    last.focus()
  } else if (!event.shiftKey && document.activeElement === last) {
    event.preventDefault()
    first.focus()
  }
}

watch(
  () => props.open,
  async (open) => {
    if (open) {
      previouslyFocused = document.activeElement as HTMLElement | null
      await nextTick()
      focusableElements()[0]?.focus()
    } else {
      previouslyFocused?.focus()
      previouslyFocused = null
    }
  },
)
</script>

<template>
  <div v-if="open" class="dialog-backdrop" role="presentation" @keydown="onKeydown">
    <div
      ref="dialogRef"
      class="dialog"
      role="dialog"
      aria-modal="true"
      aria-labelledby="confirm-dialog-title"
    >
      <h3 id="confirm-dialog-title" class="dialog-title">{{ title }}</h3>
      <p class="dialog-body" style="margin: 0">{{ body }}</p>
      <div class="dialog-actions">
        <button class="btn btn-secondary" type="button" style="min-height: 44px" @click="emit('cancel')">
          {{ cancelLabel }}
        </button>
        <button class="btn btn-primary" type="button" style="min-height: 44px" @click="emit('confirm')">
          {{ confirmLabel }}
        </button>
      </div>
    </div>
  </div>
</template>
