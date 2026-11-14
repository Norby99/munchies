<script setup lang="ts">
// Reusable variation-group editor. A variation is a group of options (e.g. "Dough" -> "Normal" /
// "Gluten-free", each with a price delta) attached either to a category or to a single dish; the
// same editor is used in both places. Every button is type="button": the editor is always rendered
// inside a form, and must not submit it.

import { PlusIcon, XIcon } from '@lucide/vue'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

import type { Variation } from '@/types'

const model = defineModel<Variation[]>({ required: true })

function renameGroup(index: number, name: string): void {
  model.value = model.value.map((group, i) => (i === index ? { ...group, name } : group))
}

function removeGroup(index: number): void {
  model.value = model.value.filter((_, i) => i !== index)
}

function addGroup(): void {
  model.value = [...model.value, { name: '', options: [{ name: '', additionalPrice: '0.0' }] }]
}

function addOption(groupIndex: number): void {
  model.value = model.value.map((group, i) =>
    i === groupIndex
      ? { ...group, options: [...group.options, { name: '', additionalPrice: '0.0' }] }
      : group,
  )
}

function renameOption(groupIndex: number, optionIndex: number, name: string): void {
  model.value = model.value.map((group, i) =>
    i === groupIndex
      ? { ...group, options: group.options.map((o, j) => (j === optionIndex ? { ...o, name } : o)) }
      : group,
  )
}

function repriceOption(groupIndex: number, optionIndex: number, additionalPrice: string): void {
  model.value = model.value.map((group, i) =>
    i === groupIndex
      ? {
          ...group,
          options: group.options.map((o, j) => (j === optionIndex ? { ...o, additionalPrice } : o)),
        }
      : group,
  )
}

function removeOption(groupIndex: number, optionIndex: number): void {
  model.value = model.value.map((group, i) =>
    i === groupIndex ? { ...group, options: group.options.filter((_, j) => j !== optionIndex) } : group,
  )
}
</script>

<template>
  <div class="flex flex-col gap-3">
    <fieldset
      v-for="(group, groupIndex) in model"
      :key="groupIndex"
      class="flex flex-col gap-2 rounded-xl border bg-card p-3"
    >
      <legend class="sr-only">Option group {{ groupIndex + 1 }}</legend>
      <div class="flex items-center gap-2">
        <Input
          class="h-9 font-medium"
          :model-value="group.name"
          placeholder="Group name, e.g. Size"
          :aria-label="`Name of option group ${groupIndex + 1}`"
          @change="renameGroup(groupIndex, ($event.target as HTMLInputElement).value)"
        />
        <Button type="button" variant="ghost" size="icon" :aria-label="`Remove option group ${group.name || groupIndex + 1}`" @click="removeGroup(groupIndex)">
          <XIcon />
        </Button>
      </div>

      <div
        v-for="(option, optionIndex) in group.options"
        :key="optionIndex"
        class="grid grid-cols-[minmax(0,1fr)_6rem_2rem] items-center gap-2 pl-3"
      >
        <Input
          class="h-9"
          :model-value="option.name"
          placeholder="Option, e.g. Large"
          :aria-label="`Option ${optionIndex + 1} name`"
          @change="renameOption(groupIndex, optionIndex, ($event.target as HTMLInputElement).value)"
        />
        <div class="relative">
          <span class="pointer-events-none absolute top-1/2 left-2.5 -translate-y-1/2 text-xs text-muted-foreground" aria-hidden="true">+&euro;</span>
          <Input
            class="h-9 pl-7 text-right tabular-nums"
            :model-value="option.additionalPrice"
            inputmode="decimal"
            :aria-label="`Option ${optionIndex + 1} extra price in euros`"
            @change="repriceOption(groupIndex, optionIndex, ($event.target as HTMLInputElement).value)"
          />
        </div>
        <Button
          type="button"
          variant="ghost"
          size="icon-sm"
          :aria-label="`Remove option ${option.name || optionIndex + 1}`"
          @click="removeOption(groupIndex, optionIndex)"
        >
          <XIcon />
        </Button>
      </div>

      <Button type="button" variant="ghost" size="sm" class="self-start" @click="addOption(groupIndex)">
        <PlusIcon data-icon="inline-start" /> Add option
      </Button>
    </fieldset>

    <Button type="button" variant="outline" size="sm" class="self-start" @click="addGroup">
      <PlusIcon data-icon="inline-start" /> Add option group
    </Button>
  </div>
</template>
