<script setup lang="ts">
// Manager menu builder. Three columns from the xl breakpoint (stacked below): the menu list, the
// selected menu (availability rules and categories), and the selected item's editor.

import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import { ArrowLeftIcon, MousePointerClickIcon, PlusIcon, SquareMenuIcon, TrashIcon } from '@lucide/vue'

import CategoryTree from '@/components/CategoryTree.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import ItemInspector from '@/components/ItemInspector.vue'
import MenuList from '@/components/MenuList.vue'
import PageHeader from '@/components/PageHeader.vue'
import ValidityEditor from '@/components/ValidityEditor.vue'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Empty, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from '@/components/ui/empty'
import { Field, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import * as restaurantsApi from '@/api/restaurants'
import { ApiError } from '@/api/client'
import { useMenusStore } from '@/stores/menus'
import { useRestaurantsStore } from '@/stores/restaurants'
import { isValid } from '@/utils/validity'
import type { MenuItemInput } from '@/api/menus'
import type { Menu, Validity } from '@/types'

const props = defineProps<{ restaurantId: string }>()

const restaurants = useRestaurantsStore()
const menus = useMenusStore()
const router = useRouter()

const restaurantName = ref('')
// Set once the menu list has been fetched, so the empty state does not flash while loading.
const loaded = ref(false)
const newMenuOpen = ref(false)
const newMenuName = ref('')
const deleteTarget = ref<Menu | null>(null)
const deleteError = ref<string | null>(null)
const addItemError = ref<string | null>(null)
const saveItemError = ref<string | null>(null)
const validityEditorRef = ref<InstanceType<typeof ValidityEditor> | null>(null)

const summaries = computed(() => menus.summaries[props.restaurantId] ?? [])
const activeMenu = computed(() => (menus.activeMenuId ? menus.cache[menus.activeMenuId] ?? null : null))
const activeItem = computed(() => {
  if (!activeMenu.value || !menus.activeItemId) return null
  for (const category of activeMenu.value.categories) {
    const found = category.items.find((item) => item.id === menus.activeItemId)
    if (found) return found
  }
  return null
})

async function load(): Promise<void> {
  restaurants.setActive(props.restaurantId)
  const cached = restaurants.items.find((r) => r.id === props.restaurantId)
  if (cached) restaurantName.value = cached.name
  else {
    const restaurant = await restaurantsApi.getRestaurant(props.restaurantId)
    restaurantName.value = restaurant.name
    restaurants.cacheOne(restaurant)
  }

  await menus.fetchSummaries(props.restaurantId)
  loaded.value = true
  const first = summaries.value[0]
  if (first) await selectMenu(first.id)
}

onMounted(load)
watch(() => props.restaurantId, load)

async function selectMenu(menuId: string): Promise<void> {
  // Flush against the *current* activeMenu before switching — validityEditorRef
  // is still the editor for the menu we're leaving at this point. Waiting for
  // it to unmount and flush from there would be one tick too late: by then
  // menus.setActiveMenu() below has already moved activeMenu on, so
  // saveValidity() would attribute the flushed edit to the *new* menu.
  validityEditorRef.value?.flush()
  menus.setActiveMenu(menuId)
  await menus.fetchFull(props.restaurantId, menuId)
}

async function createMenu(): Promise<void> {
  if (!newMenuName.value.trim()) return
  const menu = await menus.createMenu(props.restaurantId, newMenuName.value.trim())
  newMenuOpen.value = false
  newMenuName.value = ''
  await selectMenu(menu.id)
}

async function confirmDeleteMenu(): Promise<void> {
  if (!deleteTarget.value) return
  deleteError.value = null
  try {
    await menus.deleteMenu(props.restaurantId, deleteTarget.value.id)
    deleteTarget.value = null
  } catch (err) {
    deleteError.value = err instanceof ApiError ? err.message : 'Something went wrong.'
  }
}

async function saveValidity(validity: Validity[]): Promise<void> {
  if (!activeMenu.value) return
  await menus.updateMenu(props.restaurantId, activeMenu.value.id, activeMenu.value.name, validity)
}

async function onAddItem(categoryId: string, input: MenuItemInput): Promise<void> {
  if (!activeMenu.value) return
  addItemError.value = null
  try {
    await menus.createItem(props.restaurantId, activeMenu.value.id, categoryId, input)
    const category = activeMenu.value.categories.find((c) => c.id === categoryId)
    const created = category?.items[category.items.length - 1]
    if (created) menus.setActiveItem(created.id)
  } catch (err) {
    addItemError.value = err instanceof ApiError ? err.message : 'Something went wrong.'
  }
}

async function onSaveItem(input: MenuItemInput): Promise<void> {
  if (!activeMenu.value || !menus.activeItemId) return
  const category = activeMenu.value.categories.find((c) => c.items.some((i) => i.id === menus.activeItemId))
  if (!category) return
  saveItemError.value = null
  try {
    await menus.updateItem(props.restaurantId, activeMenu.value.id, category.id, menus.activeItemId, input)
  } catch (err) {
    saveItemError.value = err instanceof ApiError ? err.message : 'Something went wrong.'
  }
}

async function onDeleteItem(): Promise<void> {
  if (!activeMenu.value || !menus.activeItemId) return
  const category = activeMenu.value.categories.find((c) => c.items.some((i) => i.id === menus.activeItemId))
  if (!category) return
  await menus.removeItem(props.restaurantId, activeMenu.value.id, category.id, menus.activeItemId)
}

</script>

<template>
  <div>
    <Button variant="ghost" class="mb-4 -ml-2.5" @click="router.push({ name: 'manager-restaurants' })">
      <ArrowLeftIcon data-icon="inline-start" /> All restaurants
    </Button>
    <PageHeader eyebrow="Menus" :title="restaurantName" description="Organize menus, categories and dishes. Changes are visible to customers right away.">
      <template #actions>
        <Button @click="newMenuOpen = true"><PlusIcon data-icon="inline-start" /> New menu</Button>
      </template>
    </PageHeader>

    <Empty v-if="loaded && summaries.length === 0" class="border border-dashed bg-card">
      <EmptyHeader>
        <EmptyMedia variant="icon"><SquareMenuIcon /></EmptyMedia>
        <EmptyTitle>No menus yet</EmptyTitle>
        <EmptyDescription>Create a menu (for example "Lunch" or "Dinner"), then fill it with categories and dishes.</EmptyDescription>
      </EmptyHeader>
      <Button @click="newMenuOpen = true"><PlusIcon data-icon="inline-start" /> New menu</Button>
    </Empty>

    <div v-else class="grid items-start gap-6 xl:grid-cols-[220px_minmax(0,1fr)_340px]">
      <MenuList :menus="summaries" :cache="menus.cache" :active-id="menus.activeMenuId" @select="selectMenu" />

      <Card v-if="activeMenu">
        <CardHeader>
          <CardTitle class="flex flex-wrap items-center gap-2 text-xl font-bold">
            {{ activeMenu.name }}
            <Badge v-if="isValid(activeMenu.validity)" variant="outline" class="text-success">Open now</Badge>
            <Badge v-else variant="outline" class="text-muted-foreground">Closed now</Badge>
          </CardTitle>
          <CardDescription>Set when the menu is available, then add its categories and dishes.</CardDescription>
          <CardAction>
            <Button variant="ghost" size="icon" :aria-label="`Delete menu ${activeMenu.name}`" @click="deleteTarget = activeMenu">
              <TrashIcon />
            </Button>
          </CardAction>
        </CardHeader>
        <CardContent class="flex flex-col gap-6">
          <ValidityEditor
            ref="validityEditorRef"
            :key="activeMenu.id"
            :model-value="activeMenu.validity"
            @update:model-value="saveValidity"
          />

          <CategoryTree
            :categories="activeMenu.categories"
            :expanded="menus.expandedCategories"
            :active-item-id="menus.activeItemId"
            :add-item-error="addItemError"
            @toggle="menus.toggleCategory"
            @select-item="menus.setActiveItem"
            @add-item="onAddItem"
            @clear-add-item-error="addItemError = null"
            @delete-category="(categoryId) => menus.deleteCategory(props.restaurantId, activeMenu!.id, categoryId)"
            @add-category="
              (input) => menus.createCategory(props.restaurantId, activeMenu!.id, input.name, input.variations)
            "
            @update-category="
              (categoryId, input) =>
                menus.updateCategory(props.restaurantId, activeMenu!.id, categoryId, input.name, input.variations)
            "
          />
        </CardContent>
      </Card>
      <p v-else class="rounded-xl border border-dashed p-6 text-center text-sm text-muted-foreground">
        Select a menu to edit it.
      </p>

      <Card class="xl:sticky xl:top-24">
        <CardContent v-if="activeItem">
          <ItemInspector
            :item="activeItem"
            :save-error="saveItemError"
            @save="onSaveItem"
            @delete="onDeleteItem"
            @clear-save-error="saveItemError = null"
          />
        </CardContent>
        <CardContent v-else class="flex flex-col items-center gap-2 py-8 text-center">
          <span class="grid size-10 place-items-center rounded-lg bg-muted text-muted-foreground">
            <MousePointerClickIcon class="size-5" aria-hidden="true" />
          </span>
          <p class="font-semibold">No dish selected</p>
          <p class="text-sm text-muted-foreground">Select a dish in a category to edit its details.</p>
        </CardContent>
      </Card>
    </div>

    <Dialog v-model:open="newMenuOpen">
      <DialogContent class="sm:max-w-sm">
        <form class="contents" @submit.prevent="createMenu">
          <DialogHeader>
            <DialogTitle>New menu</DialogTitle>
            <DialogDescription>You can set its availability after creating it.</DialogDescription>
          </DialogHeader>
          <Field>
            <FieldLabel for="new-menu-name">Menu name</FieldLabel>
            <Input id="new-menu-name" v-model="newMenuName" class="h-10" type="text" placeholder="Lunch" required />
          </Field>
          <DialogFooter>
            <Button type="button" variant="outline" @click="newMenuOpen = false">Cancel</Button>
            <Button type="submit" :disabled="!newMenuName.trim()">Create menu</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>

    <ConfirmDialog
      :open="deleteTarget !== null"
      title="Delete this menu?"
      :body="deleteError ?? `${deleteTarget?.name ?? ''} and its categories will be removed permanently.`"
      confirm-label="Delete permanently"
      cancel-label="Keep menu"
      @cancel="
        () => {
          deleteTarget = null
          deleteError = null
        }
      "
      @confirm="confirmDeleteMenu"
    />
  </div>
</template>
