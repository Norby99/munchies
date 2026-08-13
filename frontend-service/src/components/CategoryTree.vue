<script setup lang="ts">
// Builder's middle column category list: each category is a disclosure
// holding its items, with an inline add/edit form reusing
// VariationGroupsEditor — the same group editor the item inspector uses.
// No position field exists on categories or items, so order is whatever the
// server returns; there are no drag handles until the backend adds one.

import { reactive, ref } from 'vue'
import { ChevronDown, ChevronRight } from 'lucide-vue-next'

import VariationGroupsEditor from '@/components/VariationGroupsEditor.vue'
import { formatEUR } from '@/utils/money'
import type { Category, Variation } from '@/types'

const props = defineProps<{
  categories: Category[]
  expanded: Set<string>
  activeItemId: string | null
}>()

const emit = defineEmits<{
  toggle: [categoryId: string]
  selectItem: [itemId: string]
  addItem: [categoryId: string]
  deleteCategory: [categoryId: string]
  addCategory: [input: { name: string; variations: Variation[] }]
  updateCategory: [categoryId: string, input: { name: string; variations: Variation[] }]
}>()

const formMode = ref<'add' | string | null>(null)
const formDraft = reactive<{ name: string; variations: Variation[] }>({ name: '', variations: [] })

function openAddForm(): void {
  formMode.value = 'add'
  formDraft.name = ''
  formDraft.variations = []
}

function openEditForm(category: Category): void {
  formMode.value = category.id
  formDraft.name = category.name
  formDraft.variations = category.variations.map((v) => ({ ...v, options: v.options.map((o) => ({ ...o })) }))
}

function cancelForm(): void {
  formMode.value = null
}

function submitForm(): void {
  const input = { name: formDraft.name, variations: formDraft.variations }
  if (formMode.value === 'add') emit('addCategory', input)
  else if (formMode.value) emit('updateCategory', formMode.value, input)
  formMode.value = null
}
</script>

<template>
  <div>
    <h6 style="margin-bottom: 10px">Categories</h6>

    <div v-for="category in props.categories" :key="category.id" class="category-tree__row">
      <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap">
        <button
          class="btn btn-ghost btn-icon"
          type="button"
          :aria-expanded="props.expanded.has(category.id)"
          :aria-label="props.expanded.has(category.id) ? 'Collapse category' : 'Expand category'"
          @click="emit('toggle', category.id)"
        >
          <ChevronDown v-if="props.expanded.has(category.id)" :size="18" />
          <ChevronRight v-else :size="18" />
        </button>
        <strong style="font-size: 16px; margin-right: auto">{{ category.name }}</strong>
        <span class="tag tag-neutral" style="flex: none; white-space: nowrap">
          {{ category.items.length }} {{ category.items.length === 1 ? 'item' : 'items' }}
        </span>
        <span class="tag tag-accent" style="flex: none; white-space: nowrap">
          {{ category.variations.length }} {{ category.variations.length === 1 ? 'group' : 'groups' }}
        </span>
        <button class="btn btn-ghost" type="button" style="flex: none; white-space: nowrap" @click="openEditForm(category)">
          Edit
        </button>
        <button
          class="btn btn-ghost"
          type="button"
          style="flex: none; white-space: nowrap"
          @click="emit('deleteCategory', category.id)"
        >
          Delete
        </button>
      </div>

      <div v-if="formMode === category.id" class="category-tree__form">
        <div class="field" style="margin-bottom: 10px">
          <label :for="`category-name-${category.id}`">Category name</label>
          <input :id="`category-name-${category.id}`" class="input" type="text" v-model="formDraft.name" />
        </div>
        <VariationGroupsEditor v-model="formDraft.variations" />
        <div style="display: flex; gap: 8px; margin-top: 10px">
          <button class="btn btn-primary" type="button" @click="submitForm">Save category</button>
          <button class="btn btn-secondary" type="button" @click="cancelForm">Cancel</button>
        </div>
      </div>

      <div v-if="props.expanded.has(category.id)" class="category-tree__items">
        <button
          v-for="item in category.items"
          :key="item.id"
          type="button"
          class="category-tree__item"
          :class="{ 'category-tree__item--active': item.id === props.activeItemId }"
          :aria-pressed="item.id === props.activeItemId"
          @click="emit('selectItem', item.id)"
        >
          <span style="margin-right: auto">{{ item.name }}</span>
          <span class="text-muted" style="font-size: 12px">
            {{ item.variations.map((v) => v.name).join(' · ') }}
          </span>
          <strong style="font-variant-numeric: tabular-nums; flex: none; white-space: nowrap">
            {{ formatEUR(item.price) }}
          </strong>
        </button>
        <button
          class="btn btn-secondary"
          type="button"
          style="align-self: flex-start; margin-top: 8px"
          @click="emit('addItem', category.id)"
        >
          Add item
        </button>
      </div>
    </div>

    <div class="category-tree__footer">
      <button v-if="formMode !== 'add'" class="btn btn-secondary" type="button" @click="openAddForm">
        Add category
      </button>
      <div v-else class="category-tree__form">
        <div class="field" style="margin-bottom: 10px">
          <label for="new-category-name">Category name</label>
          <input id="new-category-name" class="input" type="text" v-model="formDraft.name" />
        </div>
        <VariationGroupsEditor v-model="formDraft.variations" />
        <div style="display: flex; gap: 8px; margin-top: 10px">
          <button class="btn btn-primary" type="button" @click="submitForm">Save category</button>
          <button class="btn btn-secondary" type="button" @click="cancelForm">Cancel</button>
        </div>
      </div>
    </div>

    <p class="text-muted" style="font-size: 11px; margin-top: 14px">
      No position field exists — order is whatever the server returns. No drag handles until the
      backend adds one.
    </p>
  </div>
</template>

<style scoped>
.category-tree__row {
  border-top: 2px solid var(--color-divider);
  padding: 12px 0;
}

.category-tree__form {
  margin: 10px 0 0 34px;
}

.category-tree__items {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin: 10px 0 0 34px;
}

.category-tree__item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  text-align: left;
  font: inherit;
  font-size: 14px;
  cursor: pointer;
  padding: 10px 12px;
  border: 0;
  border-left: 2px solid transparent;
  background: transparent;
  color: inherit;
  justify-content: space-between;
}

.category-tree__item--active {
  border-left-color: var(--color-accent);
  background: var(--color-neutral-200);
}

.category-tree__footer {
  border-top: 2px solid var(--color-divider);
  padding-top: 12px;
}
</style>
