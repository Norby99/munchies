<script setup lang="ts">
// Customer — restaurant detail (README screen 6). Reachable by direct link,
// QR, or known id only — there is no browse/search endpoint. Every tab needs
// its availability up front, so every menu is fetched in full, not just the
// active one (see MenuTabs).
//
// Checkout intentionally has no destination here: the seven screens this
// handoff designs stop at the order draft (see README overview) — placing an
// order needs per-type delivery/takeaway/dine-in details that no screen in
// this pass defines, matching the prototype's own no-op Checkout handler.

import { computed, onMounted, ref, watch } from 'vue'

import MenuItemRow from '@/components/MenuItemRow.vue'
import MenuTabs from '@/components/MenuTabs.vue'
import NeedsBackendNote from '@/components/NeedsBackendNote.vue'
import OrderDraft from '@/components/OrderDraft.vue'
import StateBlock from '@/components/StateBlock.vue'
import { useOnline } from '@/composables/useOnline'
import * as restaurantsApi from '@/api/restaurants'
import { useCartStore } from '@/stores/cart'
import { useMenusStore } from '@/stores/menus'
import { useRestaurantsStore } from '@/stores/restaurants'
import { describe, isValid } from '@/utils/validity'
import type { Menu } from '@/types'

const props = defineProps<{ restaurantId: string }>()

const restaurants = useRestaurantsStore()
const menus = useMenusStore()
const cart = useCartStore()
const { isOnline } = useOnline()

const restaurantName = ref('')
const restaurantMeta = ref('')
const status = ref<'ready' | 'loading' | 'error'>('loading')
const loadedMenus = ref<Menu[]>([])

const activeMenu = computed(() => loadedMenus.value.find((m) => m.id === menus.activeMenuId) ?? null)
const menuOpen = computed(() => (activeMenu.value ? isValid(activeMenu.value.validity) : false))
const orderingDisabled = computed(() => !menuOpen.value || !isOnline.value)
const checkoutDisabled = computed(() => orderingDisabled.value || cart.isEmpty)

async function load(): Promise<void> {
  status.value = 'loading'
  try {
    const restaurant = await restaurantsApi.getRestaurant(props.restaurantId)
    restaurantName.value = restaurant.name
    restaurantMeta.value = `${restaurant.address} · ${restaurant.phone}`
    restaurants.cacheOne(restaurant)
    cart.setRestaurant(props.restaurantId)

    await menus.fetchSummaries(props.restaurantId)
    const summaries = menus.summaries[props.restaurantId] ?? []
    loadedMenus.value = await Promise.all(
      summaries.map((summary) => menus.fetchFull(props.restaurantId, summary.id)),
    )

    const firstOpen = loadedMenus.value.find((menu) => isValid(menu.validity))
    const initial = firstOpen ?? loadedMenus.value[0]
    if (initial) menus.setActiveMenu(initial.id)

    status.value = loadedMenus.value.length === 0 ? 'error' : 'ready'
  } catch {
    status.value = 'error'
  }
}

onMounted(load)
watch(() => props.restaurantId, load)

function selectMenu(menuId: string): void {
  menus.setActiveMenu(menuId)
}

function addToCart(itemId: string): void {
  cart.add(itemId)
}

function checkout(): void {
  // No-op by design — see the top-of-file note.
}
</script>

<template>
  <div>
    <div style="display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end">
      <div style="margin-right: auto">
        <h6 style="color: var(--color-accent)">Customer</h6>
        <h2 style="margin: 0">{{ restaurantName }}</h2>
        <p class="text-muted" style="font-size: 13px; margin: 4px 0 0">{{ restaurantMeta }}</p>
      </div>
    </div>

    <NeedsBackendNote>
      reachable by direct link, QR or known id only — there is no browse or search endpoint for
      customers yet.
    </NeedsBackendNote>

    <div class="hr"></div>

    <StateBlock
      :status="status === 'loading' ? 'loading' : status === 'error' ? 'error' : loadedMenus.length === 0 ? 'empty' : 'ready'"
      empty-title="This restaurant has no published menus"
      empty-body="The list endpoint returned an empty array. Customers see this whenever a manager has not published anything yet."
      empty-action-label="Reload"
      @retry="load"
      @empty-action="load"
    >
      <div class="restaurant-detail__grid">
        <div>
          <MenuTabs :menus="loadedMenus" :active-id="menus.activeMenuId" @select="selectMenu" />

          <div v-if="activeMenu && !menuOpen" role="status" class="restaurant-detail__notice">
            <strong style="font-size: 13px; display: block">Not available right now</strong>
            <span style="font-size: 13px; color: var(--color-accent-800)">
              {{ describe(activeMenu.validity) }}. You can still browse; ordering is disabled.
            </span>
          </div>

          <section v-for="category in activeMenu?.categories ?? []" :key="category.id" style="margin-bottom: 28px">
            <div class="restaurant-detail__category-header">
              <h4 style="margin: 0">{{ category.name }}</h4>
              <span class="text-muted" style="font-size: 12px">
                {{
                  category.variations.length
                    ? `Category variations: ${category.variations.map((v) => v.name).join(', ')}`
                    : 'No category variations'
                }}
              </span>
            </div>
            <MenuItemRow
              v-for="item in category.items"
              :key="item.id"
              :item="item"
              :disabled="orderingDisabled"
              @add="addToCart(item.id)"
            />
          </section>
        </div>

        <OrderDraft v-if="activeMenu" :menu="activeMenu" :checkout-disabled="checkoutDisabled" @checkout="checkout" />
      </div>
    </StateBlock>
  </div>
</template>

<style scoped>
.restaurant-detail__grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 28px;
  align-items: start;
}

@media (min-width: 1150px) {
  .restaurant-detail__grid {
    grid-template-columns: minmax(0, 1fr) minmax(280px, 320px);
  }
}

@media (min-width: 1440px) {
  .restaurant-detail__grid {
    gap: 40px;
  }
}

.restaurant-detail__notice {
  border-left: 2px solid var(--color-accent);
  background: var(--color-accent-100);
  padding: 12px 14px;
  margin-bottom: 20px;
}

.restaurant-detail__category-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  border-bottom: 2px solid var(--color-divider);
  padding-bottom: 8px;
  margin-bottom: 4px;
}
</style>
