<script setup lang="ts">
// Name/description/price/variation-groups fields shared by ItemInspector
// (editing an existing item) and CategoryTree's inline "Add item" form
// (creating a new one). idPrefix keeps element ids unique when several of
// these render on the same page (one per category, plus the inspector).

import VariationGroupsEditor from '@/components/VariationGroupsEditor.vue'
import type { MenuItemInput } from '@/api/menus'

defineProps<{ idPrefix: string }>()
const draft = defineModel<MenuItemInput>({ required: true })
</script>

<template>
  <div>
    <div class="field" style="margin-bottom: 12px">
      <label :for="`${idPrefix}-name`">Name</label>
      <input :id="`${idPrefix}-name`" class="input" type="text" v-model="draft.name" maxlength="150" />
      <p class="text-muted" style="font-size: 11px; margin: 4px 0 0">{{ draft.name.length }} / 150</p>
    </div>

    <div class="field" style="margin-bottom: 12px">
      <label :for="`${idPrefix}-description`">Description</label>
      <textarea :id="`${idPrefix}-description`" class="input" maxlength="500" v-model="draft.description"></textarea>
      <p class="text-muted" style="font-size: 11px; margin: 4px 0 0">{{ draft.description.length }} / 500</p>
    </div>

    <div class="field" style="margin-bottom: 16px">
      <label :for="`${idPrefix}-price`">Price</label>
      <input :id="`${idPrefix}-price`" class="input" type="text" inputmode="decimal" v-model="draft.price" />
      <p class="text-muted" style="font-size: 11px; margin: 4px 0 0">
        Decimal string on the wire. Never parsed to a float before display.
      </p>
    </div>

    <div class="hr" style="margin: 16px 0"></div>
    <h6 style="margin-bottom: 10px">Variation groups</h6>
    <VariationGroupsEditor v-model="draft.variations" />
  </div>
</template>
