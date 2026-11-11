<script setup lang="ts">
// Menu builder's dish editor. PUT replaces the whole item, so Save always submits every field,
// including untouched variations: there is no partial-update endpoint.

import { reactive, ref, watch } from 'vue'
import { TrashIcon } from '@lucide/vue'

import ConfirmDialog from '@/components/ConfirmDialog.vue'
import MenuItemFieldsForm from '@/components/MenuItemFieldsForm.vue'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import type { MenuItemInput } from '@/api/menus'
import type { MenuItem } from '@/types'

const props = defineProps<{ item: MenuItem; saveError: string | null }>()
const emit = defineEmits<{ save: [input: MenuItemInput]; delete: []; clearSaveError: [] }>()

function draftFrom(item: MenuItem): MenuItemInput {
  return {
    name: item.name,
    description: item.description,
    price: item.price,
    variations: item.variations.map((v) => ({ name: v.name, options: v.options.map((o) => ({ ...o })) })),
  }
}

const draft = reactive<MenuItemInput>(draftFrom(props.item))

watch(
  () => props.item,
  (item) => {
    Object.assign(draft, draftFrom(item))
    emit('clearSaveError')
  },
)

function onSave(): void {
  emit('save', { ...draft, variations: draft.variations.map((v) => ({ ...v, options: v.options.map((o) => ({ ...o })) })) })
}

const deleteOpen = ref(false)

function confirmDelete(): void {
  emit('delete')
  deleteOpen.value = false
}
</script>

<template>
  <form class="flex flex-col gap-5" @submit.prevent="onSave">
    <div class="flex items-center gap-2">
      <h3 class="min-w-0 flex-1 truncate text-base font-bold">Edit dish</h3>
      <Button type="button" variant="ghost" size="icon" :aria-label="`Delete ${props.item.name}`" @click="deleteOpen = true">
        <TrashIcon />
      </Button>
    </div>

    <MenuItemFieldsForm id-prefix="item" v-model="draft" />

    <Alert v-if="saveError" variant="destructive" role="alert">
      <AlertDescription>{{ saveError }}</AlertDescription>
    </Alert>

    <Button type="submit" class="w-full">Save dish</Button>

    <ConfirmDialog
      :open="deleteOpen"
      title="Delete this dish?"
      :body="`${props.item.name} will be removed from the menu permanently.`"
      confirm-label="Delete permanently"
      cancel-label="Keep dish"
      @cancel="deleteOpen = false"
      @confirm="confirmDelete"
    />
  </form>
</template>
