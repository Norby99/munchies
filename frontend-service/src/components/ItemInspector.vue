<script setup lang="ts">
// Builder's right column: the selected item's editable form. PUT replaces
// the whole item, so Save always submits every field, including untouched
// variations — there is no partial-update endpoint.

import { reactive, watch } from 'vue'

import VariationGroupsEditor from '@/components/VariationGroupsEditor.vue'
import type { MenuItemInput } from '@/api/menus'
import type { MenuItem } from '@/types'

const props = defineProps<{ item: MenuItem }>()
const emit = defineEmits<{ save: [input: MenuItemInput] }>()

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
</script>

<template>
  <div>
    <h6 style="margin-bottom: 12px">Item</h6>

    <div class="field" style="margin-bottom: 12px">
      <label for="item-name">Name</label>
      <input id="item-name" class="input" type="text" v-model="draft.name" maxlength="150" />
      <p class="text-muted" style="font-size: 11px; margin: 4px 0 0">{{ draft.name.length }} / 150</p>
    </div>

    <div class="field" style="margin-bottom: 12px">
      <label for="item-description">Description</label>
      <textarea id="item-description" class="input" maxlength="500" v-model="draft.description"></textarea>
      <p class="text-muted" style="font-size: 11px; margin: 4px 0 0">{{ draft.description.length }} / 500</p>
    </div>

    <div class="field" style="margin-bottom: 16px">
      <label for="item-price">Price</label>
      <input id="item-price" class="input" type="text" inputmode="decimal" v-model="draft.price" />
      <p class="text-muted" style="font-size: 11px; margin: 4px 0 0">
        Decimal string on the wire. Never parsed to a float before display.
      </p>
    </div>

    <div class="hr" style="margin: 16px 0"></div>
    <h6 style="margin-bottom: 10px">Variation groups</h6>
    <VariationGroupsEditor v-model="draft.variations" />

    <div class="hr" style="margin: 16px 0"></div>
    <button class="btn btn-primary btn-block" type="button" @click="onSave">Save item</button>
    <p class="text-muted" style="font-size: 11px; margin-top: 10px">
      PUT replaces the whole item, so the form always submits every field — including untouched
      variations.
    </p>
  </div>
</template>
