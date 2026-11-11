<script setup lang="ts">
// Menu builder's category list: each category is a disclosure holding its dishes, with inline
// add/edit forms reusing VariationGroupsEditor (the same group editor the item inspector uses).
// Categories and items have no position field, so their order is the one the server returns.

import { reactive, ref, watch } from 'vue'
import { ChevronRightIcon, EllipsisVerticalIcon, FolderPlusIcon, PencilIcon, PlusIcon, TrashIcon } from '@lucide/vue'

import ConfirmDialog from '@/components/ConfirmDialog.vue'
import MenuItemFieldsForm from '@/components/MenuItemFieldsForm.vue'
import VariationGroupsEditor from '@/components/VariationGroupsEditor.vue'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Field, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { cn } from '@/lib/utils'
import { formatEUR } from '@/utils/money'
import type { MenuItemInput } from '@/api/menus'
import type { Category, Variation } from '@/types'

const props = defineProps<{
  categories: Category[]
  expanded: Set<string>
  activeItemId: string | null
  addItemError: string | null
}>()

const emit = defineEmits<{
  toggle: [categoryId: string]
  selectItem: [itemId: string | null]
  addItem: [categoryId: string, input: MenuItemInput]
  clearAddItemError: []
  deleteCategory: [categoryId: string]
  addCategory: [input: { name: string; variations: Variation[] }]
  updateCategory: [categoryId: string, input: { name: string; variations: Variation[] }]
}>()

const formMode = ref<'add' | string | null>(null)
const formDraft = reactive<{ name: string; variations: Variation[] }>({ name: '', variations: [] })
const deleteTarget = ref<Category | null>(null)
const itemFormOpenFor = ref<string | null>(null)
const itemDraft = reactive<MenuItemInput>({ name: '', description: '', price: '0.00', variations: [] })

function onToggle(category: Category): void {
  const wasExpanded = props.expanded.has(category.id)
  emit('toggle', category.id)
  if (wasExpanded && category.items.some((item) => item.id === props.activeItemId)) {
    emit('selectItem', null)
  }
}

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

function confirmDelete(): void {
  if (!deleteTarget.value) return
  emit('deleteCategory', deleteTarget.value.id)
  deleteTarget.value = null
}

function openAddItemForm(categoryId: string): void {
  if (!props.expanded.has(categoryId)) emit('toggle', categoryId)
  itemFormOpenFor.value = categoryId
  itemDraft.name = ''
  itemDraft.description = ''
  itemDraft.price = '0.00'
  itemDraft.variations = []
  emit('clearAddItemError')
}

function cancelItemForm(): void {
  itemFormOpenFor.value = null
  emit('clearAddItemError')
}

function submitItemForm(categoryId: string): void {
  emit('addItem', categoryId, {
    ...itemDraft,
    variations: itemDraft.variations.map((v) => ({ ...v, options: v.options.map((o) => ({ ...o })) })),
  })
}

// Closes the add-item form once the item actually lands in `categories` —
// i.e. only on success. On failure the parent's createItem throws before
// re-fetching, categories never changes, and the form stays open with
// addItemError showing instead of closing before the user can read it.
watch(
  () => props.categories.find((c) => c.id === itemFormOpenFor.value)?.items.length,
  (length, previousLength) => {
    if (itemFormOpenFor.value && length !== undefined && previousLength !== undefined && length > previousLength) {
      itemFormOpenFor.value = null
    }
  },
)
</script>

<template>
  <section aria-labelledby="categories-title" class="flex flex-col gap-3">
    <div class="flex items-center gap-2">
      <h3 id="categories-title" class="text-base font-bold">Categories</h3>
      <Button v-if="formMode !== 'add'" variant="outline" size="sm" class="ml-auto" @click="openAddForm">
        <FolderPlusIcon data-icon="inline-start" /> Add category
      </Button>
    </div>

    <form
      v-if="formMode === 'add'"
      class="flex flex-col gap-4 rounded-xl border border-dashed bg-muted/40 p-4"
      @submit.prevent="submitForm"
    >
      <Field>
        <FieldLabel for="new-category-name">Category name</FieldLabel>
        <Input id="new-category-name" v-model="formDraft.name" class="h-10 bg-card" type="text" placeholder="Pasta" required />
      </Field>
      <VariationGroupsEditor v-model="formDraft.variations" />
      <div class="flex justify-end gap-2">
        <Button type="button" variant="outline" @click="cancelForm">Cancel</Button>
        <Button type="submit">Save category</Button>
      </div>
    </form>

    <p
      v-if="props.categories.length === 0 && formMode !== 'add'"
      class="rounded-xl border border-dashed p-6 text-center text-sm text-muted-foreground"
    >
      No categories yet. Categories group your dishes (for example "Starters" or "Pizzas") and can hold
      options shared by all of them.
    </p>

    <div v-for="category in props.categories" :key="category.id" class="rounded-xl border bg-card">
      <div class="flex items-center gap-2 p-2 pr-3">
        <Button
          variant="ghost"
          class="h-10 min-w-0 flex-1 justify-start gap-2 px-2"
          :aria-expanded="props.expanded.has(category.id)"
          @click="onToggle(category)"
        >
          <ChevronRightIcon
            :class="cn('transition-transform', props.expanded.has(category.id) && 'rotate-90')"
            aria-hidden="true"
          />
          <span class="truncate text-base font-semibold">{{ category.name }}</span>
          <Badge variant="secondary" class="tabular-nums">
            {{ category.items.length }} {{ category.items.length === 1 ? 'dish' : 'dishes' }}
          </Badge>
          <Badge v-if="category.variations.length" variant="outline" class="hidden sm:inline-flex">
            {{ category.variations.length }} {{ category.variations.length === 1 ? 'option' : 'options' }}
          </Badge>
        </Button>
        <Button variant="ghost" size="sm" @click="openAddItemForm(category.id)">
          <PlusIcon data-icon="inline-start" /> Dish
        </Button>
        <DropdownMenu>
          <DropdownMenuTrigger as-child>
            <Button variant="ghost" size="icon" :aria-label="`More actions for ${category.name}`">
              <EllipsisVerticalIcon />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuGroup>
              <DropdownMenuItem @select="openEditForm(category)"><PencilIcon /> Edit category</DropdownMenuItem>
            </DropdownMenuGroup>
            <DropdownMenuSeparator />
            <DropdownMenuGroup>
              <DropdownMenuItem variant="destructive" @select="deleteTarget = category">
                <TrashIcon /> Delete category
              </DropdownMenuItem>
            </DropdownMenuGroup>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      <form
        v-if="formMode === category.id"
        class="mx-3 mb-3 flex flex-col gap-4 rounded-xl border border-dashed bg-muted/40 p-4"
        @submit.prevent="submitForm"
      >
        <Field>
          <FieldLabel :for="`category-name-${category.id}`">Category name</FieldLabel>
          <Input :id="`category-name-${category.id}`" v-model="formDraft.name" class="h-10 bg-card" type="text" required />
        </Field>
        <VariationGroupsEditor v-model="formDraft.variations" />
        <div class="flex justify-end gap-2">
          <Button type="button" variant="outline" @click="cancelForm">Cancel</Button>
          <Button type="submit">Save category</Button>
        </div>
      </form>

      <div v-if="props.expanded.has(category.id)" class="flex flex-col gap-1 border-t p-2">
        <p v-if="category.items.length === 0 && itemFormOpenFor !== category.id" class="px-2 py-3 text-sm text-muted-foreground">
          No dishes in this category yet.
        </p>
        <button
          v-for="item in category.items"
          :key="item.id"
          type="button"
          :aria-pressed="item.id === props.activeItemId"
          :class="
            cn(
              'flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-left text-sm transition-colors outline-none focus-visible:ring-3 focus-visible:ring-ring/50',
              item.id === props.activeItemId ? 'bg-accent text-accent-foreground' : 'hover:bg-muted',
            )
          "
          @click="emit('selectItem', item.id === props.activeItemId ? null : item.id)"
        >
          <span class="min-w-0 flex-1 truncate font-medium">{{ item.name }}</span>
          <span v-if="item.variations.length" class="hidden truncate text-xs opacity-80 sm:inline">
            {{ item.variations.map((v) => v.name).join(' · ') }}
          </span>
          <span class="shrink-0 font-semibold tabular-nums">{{ formatEUR(item.price) }}</span>
        </button>

        <form
          v-if="itemFormOpenFor === category.id"
          class="m-1 flex flex-col gap-4 rounded-xl border border-dashed bg-muted/40 p-4"
          @submit.prevent="submitItemForm(category.id)"
        >
          <p class="font-semibold">New dish in {{ category.name }}</p>
          <MenuItemFieldsForm :id-prefix="`item-${category.id}`" v-model="itemDraft" />
          <Alert v-if="addItemError" variant="destructive" role="alert">
            <AlertDescription>{{ addItemError }}</AlertDescription>
          </Alert>
          <div class="flex justify-end gap-2">
            <Button type="button" variant="outline" @click="cancelItemForm">Cancel</Button>
            <Button type="submit">Add dish</Button>
          </div>
        </form>
      </div>
    </div>

    <ConfirmDialog
      :open="deleteTarget !== null"
      title="Delete this category?"
      :body="`${deleteTarget?.name ?? ''} and its dishes will be removed permanently.`"
      confirm-label="Delete permanently"
      cancel-label="Keep category"
      @cancel="deleteTarget = null"
      @confirm="confirmDelete"
    />
  </section>
</template>
