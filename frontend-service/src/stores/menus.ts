// Menu list + full menu cache for the manager builder and the customer
// detail screen. The list endpoint only returns id + name — categories and
// validity need the full menu fetch, cached here per menu id.

import { defineStore } from 'pinia'
import { reactive, ref } from 'vue'

import * as menusApi from '@/api/menus'
import type { MenuItemInput } from '@/api/menus'
import type { LoadStatus, Menu, MenuSummary, Validity, Variation } from '@/types'

export const useMenusStore = defineStore('menus', () => {
  const summaries = ref<Record<string, MenuSummary[]>>({})
  const cache = ref<Record<string, Menu>>({})
  const activeMenuId = ref<string | null>(null)
  const activeItemId = ref<string | null>(null)
  const expandedCategories = reactive(new Set<string>())
  const status = ref<LoadStatus>('ready')

  async function fetchSummaries(restaurantId: string): Promise<void> {
    status.value = 'loading'
    try {
      const list = await menusApi.listMenus(restaurantId)
      summaries.value[restaurantId] = list
      status.value = list.length === 0 ? 'empty' : 'ready'
    } catch {
      status.value = 'error'
    }
  }

  async function fetchFull(restaurantId: string, menuId: string): Promise<Menu> {
    const menu = await menusApi.getMenu(restaurantId, menuId)
    cache.value[menuId] = menu
    return menu
  }

  function toggleCategory(categoryId: string): void {
    if (expandedCategories.has(categoryId)) expandedCategories.delete(categoryId)
    else expandedCategories.add(categoryId)
  }

  async function createMenu(restaurantId: string, name: string): Promise<Menu> {
    const menu = await menusApi.createMenu(restaurantId, name)
    cache.value[menu.id] = menu
    summaries.value[restaurantId] = [...(summaries.value[restaurantId] ?? []), { id: menu.id, name: menu.name }]
    status.value = 'ready'
    return menu
  }

  async function updateMenu(
    restaurantId: string,
    menuId: string,
    name: string,
    validity: Validity[],
  ): Promise<void> {
    const menu = await menusApi.updateMenu(restaurantId, menuId, name, validity)
    cache.value[menuId] = menu
    const list = summaries.value[restaurantId]
    if (list) {
      const index = list.findIndex((m) => m.id === menuId)
      if (index !== -1) list[index] = { id: menuId, name }
    }
  }

  async function deleteMenu(restaurantId: string, menuId: string): Promise<void> {
    await menusApi.deleteMenu(restaurantId, menuId)
    delete cache.value[menuId]
    summaries.value[restaurantId] = (summaries.value[restaurantId] ?? []).filter((m) => m.id !== menuId)
  }

  async function createCategory(
    restaurantId: string,
    menuId: string,
    name: string,
    variations: Variation[] = [],
  ): Promise<void> {
    await menusApi.createCategory(restaurantId, menuId, name, variations)
    await fetchFull(restaurantId, menuId)
  }

  async function updateCategory(
    restaurantId: string,
    menuId: string,
    categoryId: string,
    name: string,
    variations: Variation[] = [],
  ): Promise<void> {
    await menusApi.updateCategory(restaurantId, menuId, categoryId, name, variations)
    await fetchFull(restaurantId, menuId)
  }

  async function deleteCategory(restaurantId: string, menuId: string, categoryId: string): Promise<void> {
    await menusApi.deleteCategory(restaurantId, menuId, categoryId)
    await fetchFull(restaurantId, menuId)
  }

  async function createItem(
    restaurantId: string,
    menuId: string,
    categoryId: string,
    input: MenuItemInput,
  ): Promise<void> {
    await menusApi.createMenuItem(restaurantId, menuId, categoryId, input)
    await fetchFull(restaurantId, menuId)
  }

  async function updateItem(
    restaurantId: string,
    menuId: string,
    categoryId: string,
    itemId: string,
    input: MenuItemInput,
  ): Promise<void> {
    await menusApi.updateMenuItem(restaurantId, menuId, categoryId, itemId, input)
    await fetchFull(restaurantId, menuId)
  }

  async function removeItem(
    restaurantId: string,
    menuId: string,
    categoryId: string,
    itemId: string,
  ): Promise<void> {
    await menusApi.removeMenuItem(restaurantId, menuId, categoryId, itemId)
    await fetchFull(restaurantId, menuId)
  }

  function setActiveMenu(menuId: string): void {
    activeMenuId.value = menuId
    activeItemId.value = null
  }

  function setActiveItem(itemId: string | null): void {
    activeItemId.value = itemId
  }

  return {
    summaries,
    cache,
    activeMenuId,
    activeItemId,
    expandedCategories,
    status,
    fetchSummaries,
    fetchFull,
    toggleCategory,
    createMenu,
    updateMenu,
    deleteMenu,
    createCategory,
    updateCategory,
    deleteCategory,
    createItem,
    updateItem,
    removeItem,
    setActiveMenu,
    setActiveItem,
  }
})
