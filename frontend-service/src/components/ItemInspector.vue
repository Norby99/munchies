<script setup lang="ts">
// Builder's right column: the selected item's editable form. PUT replaces
// the whole item, so Save always submits every field, including untouched
// variations — there is no partial-update endpoint.

import { reactive, ref, watch } from 'vue'

import ConfirmDialog from '@/components/ConfirmDialog.vue'
import MenuItemFieldsForm from '@/components/MenuItemFieldsForm.vue'
import type { MenuItemInput } from '@/api/menus'
import type { MenuItem } from '@/types'

const props = defineProps<{ item: MenuItem }>()
const emit = defineEmits<{ save: [input: MenuItemInput]; delete: [] }>()

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
  (item) => Object.assign(draft, draftFrom(item)),
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
  <div>
    <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 12px">
      <h6 style="margin: 0">Item</h6>
      <button
        class="btn btn-ghost"
        type="button"
        style="flex: none; white-space: nowrap; margin-left: auto"
        @click="deleteOpen = true"
      >
        Delete item
      </button>
    </div>

    <MenuItemFieldsForm id-prefix="item" v-model="draft" />

    <div class="hr" style="margin: 16px 0"></div>
    <button class="btn btn-primary btn-block" type="button" @click="onSave">Save item</button>
    <p class="text-muted" style="font-size: 11px; margin-top: 10px">
      PUT replaces the whole item, so the form always submits every field — including untouched
      variations.
    </p>

    <ConfirmDialog
      :open="deleteOpen"
      title="Delete this item?"
      :body="`${props.item.name} will be removed permanently.`"
      confirm-label="Delete permanently"
      cancel-label="Keep item"
      @cancel="deleteOpen = false"
      @confirm="confirmDelete"
    />
  </div>
</template>
