// The order draft. Pure client-only state — there is no server-side cart,
// and the order domain itself only ever stores menuItemId + quantity, so all
// prices/totals here are computed by joining against the loaded menu.
// Persisted to localStorage so a mid-session 401 does not lose the draft.

import { defineStore } from 'pinia'
import { computed, ref, watch } from 'vue'

import { multiplyDecimalString, addDecimalStrings, formatEUR } from '@/utils/money'
import type { Menu, MenuItem } from '@/types'

const STORAGE_KEY = 'munchies.cart'

export interface CartLine {
  itemId: string
  item: MenuItem
  quantity: number
  unitLabel: string
  totalLabel: string
}

interface PersistedCart {
  restaurantId: string | null
  items: Record<string, number>
}

function loadPersisted(): PersistedCart {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return { restaurantId: null, items: {} }
    return JSON.parse(raw) as PersistedCart
  } catch {
    return { restaurantId: null, items: {} }
  }
}

function findItem(menu: Menu, itemId: string): MenuItem | undefined {
  for (const category of menu.categories) {
    const found = category.items.find((item) => item.id === itemId)
    if (found) return found
  }
  return undefined
}

export const useCartStore = defineStore('cart', () => {
  const persisted = loadPersisted()
  const restaurantId = ref<string | null>(persisted.restaurantId)
  const items = ref<Record<string, number>>(persisted.items)

  watch(
    [restaurantId, items],
    ([nextRestaurantId, nextItems]) => {
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({ restaurantId: nextRestaurantId, items: nextItems }),
      )
    },
    { deep: true },
  )

  const isEmpty = computed(() => Object.values(items.value).every((qty) => qty <= 0))

  function setRestaurant(id: string): void {
    if (restaurantId.value !== id) {
      restaurantId.value = id
      items.value = {}
    }
  }

  function add(itemId: string): void {
    items.value[itemId] = (items.value[itemId] ?? 0) + 1
  }

  function increment(itemId: string): void {
    items.value[itemId] = (items.value[itemId] ?? 0) + 1
  }

  function decrement(itemId: string): void {
    const next = Math.max(0, (items.value[itemId] ?? 0) - 1)
    if (next === 0) delete items.value[itemId]
    else items.value[itemId] = next
  }

  function clear(): void {
    items.value = {}
  }

  function lines(menu: Menu): CartLine[] {
    return Object.entries(items.value)
      .filter(([, quantity]) => quantity > 0)
      .map(([itemId, quantity]) => {
        const item = findItem(menu, itemId)
        if (!item) return null
        return {
          itemId,
          item,
          quantity,
          unitLabel: formatEUR(item.price),
          totalLabel: formatEUR(multiplyDecimalString(item.price, quantity)),
        }
      })
      .filter((line): line is CartLine => line !== null)
  }

  function total(menu: Menu): string {
    return lines(menu).reduce((sum, line) => addDecimalStrings(sum, multiplyDecimalString(line.item.price, line.quantity)), '0.00')
  }

  function totalLabel(menu: Menu): string {
    return formatEUR(total(menu))
  }

  return {
    restaurantId,
    items,
    isEmpty,
    setRestaurant,
    add,
    increment,
    decrement,
    clear,
    lines,
    total,
    totalLabel,
  }
})
