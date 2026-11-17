<script setup lang="ts">
// Name / description / price / variation-group fields shared by ItemInspector (editing a dish) and
// CategoryTree's inline "add dish" form. idPrefix keeps element ids unique when several of these
// render on the same page (one per category, plus the inspector).

import VariationGroupsEditor from '@/components/VariationGroupsEditor.vue'
import { Field, FieldDescription, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import type { MenuItemInput } from '@/api/menus'

defineProps<{ idPrefix: string }>()
const draft = defineModel<MenuItemInput>({ required: true })
</script>

<template>
  <FieldGroup class="gap-5">
    <Field>
      <FieldLabel :for="`${idPrefix}-name`">Name</FieldLabel>
      <Input :id="`${idPrefix}-name`" v-model="draft.name" class="h-10 bg-card" type="text" maxlength="150" required />
      <FieldDescription class="text-right text-xs tabular-nums">{{ draft.name.length }} / 150</FieldDescription>
    </Field>

    <Field>
      <FieldLabel :for="`${idPrefix}-description`">Description</FieldLabel>
      <Textarea :id="`${idPrefix}-description`" v-model="draft.description" class="bg-card" maxlength="500" rows="3" />
      <FieldDescription class="text-right text-xs tabular-nums">{{ draft.description.length }} / 500</FieldDescription>
    </Field>

    <Field>
      <FieldLabel :for="`${idPrefix}-price`">Price</FieldLabel>
      <div class="relative">
        <span class="pointer-events-none absolute top-1/2 left-3 -translate-y-1/2 text-sm text-muted-foreground" aria-hidden="true">&euro;</span>
        <Input
          :id="`${idPrefix}-price`"
          v-model="draft.price"
          class="h-10 bg-card pl-7 tabular-nums"
          type="text"
          inputmode="decimal"
          placeholder="9.50"
          :aria-describedby="`${idPrefix}-price-hint`"
        />
      </div>
      <FieldDescription :id="`${idPrefix}-price-hint`">In euros, with a dot for the cents (e.g. 9.50).</FieldDescription>
    </Field>

    <div class="flex flex-col gap-3">
      <p class="text-sm font-medium">Options</p>
      <VariationGroupsEditor v-model="draft.variations" />
    </div>
  </FieldGroup>
</template>
