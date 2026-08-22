<script setup lang="ts">
// Manager — menu builder (README screen 5). Three columns: menu list,
// selected menu (validity + categories), selected item inspector.

import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import CategoryTree from '@/components/CategoryTree.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import ItemInspector from '@/components/ItemInspector.vue'
import MenuList from '@/components/MenuList.vue'
import ValidityEditor from '@/components/ValidityEditor.vue'
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
const newMenuOpen = ref(false)
const newMenuName = ref('')
const deleteTarget = ref<Menu | null>(null)
const deleteError = ref<string | null>(null)

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
  const first = summaries.value[0]
  if (first) await selectMenu(first.id)
}

onMounted(load)
watch(() => props.restaurantId, load)

async function selectMenu(menuId: string): Promise<void> {
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
  await menus.createItem(props.restaurantId, activeMenu.value.id, categoryId, input)
  const category = activeMenu.value.categories.find((c) => c.id === categoryId)
  const created = category?.items[category.items.length - 1]
  if (created) menus.setActiveItem(created.id)
}

async function onSaveItem(input: MenuItemInput): Promise<void> {
  if (!activeMenu.value || !menus.activeItemId) return
  const category = activeMenu.value.categories.find((c) => c.items.some((i) => i.id === menus.activeItemId))
  if (!category) return
  await menus.updateItem(props.restaurantId, activeMenu.value.id, category.id, menus.activeItemId, input)
}

async function onDeleteItem(): Promise<void> {
  if (!activeMenu.value || !menus.activeItemId) return
  const category = activeMenu.value.categories.find((c) => c.items.some((i) => i.id === menus.activeItemId))
  if (!category) return
  await menus.removeItem(props.restaurantId, activeMenu.value.id, category.id, menus.activeItemId)
}

const emptyCopy = {
  title: 'This menu has no categories',
  body: 'Categories hold both items and the variation groups that apply to all of them. Add one before adding items.',
  action: 'Add category',
}
</script>

<template>
  <div>
    <div style="display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end">
      <div style="margin-right: auto">
        <h6 style="color: var(--color-accent)">Menu builder</h6>
        <h2 style="margin: 0">{{ restaurantName }}</h2>
      </div>
      <button
        class="btn btn-secondary"
        type="button"
        style="flex: none; white-space: nowrap"
        @click="router.push({ name: 'manager-restaurants' })"
      >
        All restaurants
      </button>
      <button class="btn btn-primary" type="button" style="flex: none; white-space: nowrap" @click="newMenuOpen = true">
        New menu
      </button>
    </div>

    <div v-if="newMenuOpen" class="menu-builder__new-menu">
      <div class="field" style="margin-bottom: 10px">
        <label for="new-menu-name">Menu name</label>
        <input id="new-menu-name" class="input" type="text" v-model="newMenuName" />
      </div>
      <div style="display: flex; gap: 8px">
        <button class="btn btn-primary" type="button" @click="createMenu">Create menu</button>
        <button class="btn btn-secondary" type="button" @click="newMenuOpen = false">Cancel</button>
      </div>
    </div>

    <div class="hr"></div>

    <div class="menu-builder__grid">
      <MenuList :menus="summaries" :cache="menus.cache" :active-id="menus.activeMenuId" @select="selectMenu" />

      <div class="menu-builder__middle">
        <template v-if="activeMenu">
          <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 12px">
            <h4 style="margin: 0">{{ activeMenu.name }}</h4>
            <span
              class="tag"
              :class="isValid(activeMenu.validity) ? 'tag-accent' : 'tag-neutral'"
              style="flex: none; white-space: nowrap"
            >
              {{ isValid(activeMenu.validity) ? 'Available now' : 'Closed now' }}
            </span>
            <button
              class="btn btn-ghost"
              type="button"
              style="flex: none; white-space: nowrap; margin-left: auto"
              @click="deleteTarget = activeMenu"
            >
              Delete menu
            </button>
          </div>

          <ValidityEditor
            :model-value="activeMenu.validity"
            @update:model-value="saveValidity"
          />

          <p v-if="activeMenu.categories.length === 0" class="text-muted" style="font-size: 13px">
            {{ emptyCopy.body }}
          </p>

          <CategoryTree
            :categories="activeMenu.categories"
            :expanded="menus.expandedCategories"
            :active-item-id="menus.activeItemId"
            @toggle="menus.toggleCategory"
            @select-item="menus.setActiveItem"
            @add-item="onAddItem"
            @delete-category="(categoryId) => menus.deleteCategory(props.restaurantId, activeMenu!.id, categoryId)"
            @add-category="
              (input) => menus.createCategory(props.restaurantId, activeMenu!.id, input.name, input.variations)
            "
            @update-category="
              (categoryId, input) =>
                menus.updateCategory(props.restaurantId, activeMenu!.id, categoryId, input.name, input.variations)
            "
          />
        </template>
        <p v-else class="text-muted" style="font-size: 13px">Select a menu to see its categories.</p>
      </div>

      <div class="menu-builder__inspector">
        <ItemInspector v-if="activeItem" :item="activeItem" @save="onSaveItem" @delete="onDeleteItem" />
        <p v-else class="text-muted" style="font-size: 13px">Select an item to edit it.</p>
      </div>
    </div>

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

<style scoped>
.menu-builder__new-menu {
  border: 1px solid var(--color-divider);
  padding: 14px;
  margin-top: 16px;
  max-width: 360px;
}

.menu-builder__grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 24px;
  align-items: start;
}

.menu-builder__middle {
  border-top: 2px solid var(--color-divider);
  padding-top: 16px;
}

.menu-builder__inspector {
  border-top: 2px solid var(--color-divider);
  padding-top: 16px;
}

@media (min-width: 1150px) {
  .menu-builder__grid {
    grid-template-columns: minmax(170px, 200px) minmax(360px, 1fr) minmax(280px, 320px);
  }

  .menu-builder__middle {
    border-top: none;
    padding-top: 0;
    border-left: 2px solid var(--color-divider);
    border-right: 2px solid var(--color-divider);
    padding-left: 20px;
    padding-right: 20px;
  }

  .menu-builder__inspector {
    border-top: none;
    padding-top: 0;
  }
}

@media (min-width: 1440px) {
  .menu-builder__grid {
    gap: 32px;
  }

  .menu-builder__middle {
    padding-left: 28px;
    padding-right: 28px;
  }
}
</style>
