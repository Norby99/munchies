<script setup lang="ts">
// Reusable variation-group editor. Variation is a modifier group (e.g.
// "Dough" -> "Normal" / "Gluten-free", each with a price delta) attached
// either at category level or item level — ui_specification.md §5.1 asks for
// one shared editor reused in both places rather than two implementations.

import { Plus, X } from 'lucide-vue-next'

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
  <div>
    <div v-for="(group, groupIndex) in model" :key="groupIndex" class="variation-group">
      <div style="display: flex; gap: 8px; align-items: center; margin-bottom: 8px">
        <input
          class="input"
          :value="group.name"
          placeholder="Variation name"
          @change="renameGroup(groupIndex, ($event.target as HTMLInputElement).value)"
        />
        <button class="btn btn-ghost btn-icon" type="button" aria-label="Remove group" @click="removeGroup(groupIndex)">
          <X :size="16" />
        </button>
      </div>

      <div
        v-for="(option, optionIndex) in group.options"
        :key="optionIndex"
        class="variation-group__option"
      >
        <input
          class="input"
          :value="option.name"
          placeholder="Option name"
          @change="renameOption(groupIndex, optionIndex, ($event.target as HTMLInputElement).value)"
        />
        <input
          class="input"
          :value="option.additionalPrice"
          inputmode="decimal"
          aria-label="Additional price"
          style="text-align: right; font-variant-numeric: tabular-nums"
          @change="repriceOption(groupIndex, optionIndex, ($event.target as HTMLInputElement).value)"
        />
        <button
          class="btn btn-ghost btn-icon"
          type="button"
          aria-label="Remove option"
          @click="removeOption(groupIndex, optionIndex)"
        >
          <X :size="16" />
        </button>
      </div>

      <button class="btn btn-ghost" type="button" @click="addOption(groupIndex)">
        <Plus :size="14" /> Option
      </button>
    </div>

    <button class="btn btn-secondary" type="button" @click="addGroup">Add variation</button>
  </div>
</template>

<style scoped>
.variation-group {
  border: 1px solid var(--color-divider);
  padding: 10px;
  margin-bottom: 10px;
}

.variation-group__option {
  display: grid;
  grid-template-columns: 1fr 84px 32px;
  gap: 6px;
  margin-bottom: 6px;
}
</style>
