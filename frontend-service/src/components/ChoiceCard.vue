<script setup lang="ts">
// One option of a RadioGroup rendered as a selectable card (icon, title, description), following
// the shadcn-vue "choice card" composition: FieldLabel > Field > RadioGroupItem. Used for the
// account type, the order type and the payment method.

import { type Component } from 'vue'

import { Field, FieldContent, FieldDescription, FieldLabel, FieldTitle } from '@/components/ui/field'
import { RadioGroupItem } from '@/components/ui/radio-group'

defineProps<{ id: string; value: string; title: string; description?: string; icon?: Component }>()
</script>

<template>
  <FieldLabel :for="id" class="cursor-pointer">
    <Field orientation="horizontal" class="items-center gap-3">
      <span v-if="icon" class="grid size-9 shrink-0 place-items-center rounded-lg bg-muted text-foreground">
        <component :is="icon" class="size-4" aria-hidden="true" />
      </span>
      <FieldContent>
        <FieldTitle>{{ title }}</FieldTitle>
        <FieldDescription v-if="description">{{ description }}</FieldDescription>
      </FieldContent>
      <RadioGroupItem :id="id" :value="value" />
    </Field>
  </FieldLabel>
</template>
