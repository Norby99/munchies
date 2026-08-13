<script setup lang="ts">
// Manager — my restaurants (README screen 4). Menu counts aren't part of
// RestaurantDto (id, name, address, phone, email only) — restaurant-shared's
// MenuDto.kt has no aggregate count field, so this view derives the count by
// fetching each restaurant's menu summaries through the existing menus store
// rather than inventing a field on Restaurant.

import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import ConfirmDialog from '@/components/ConfirmDialog.vue'
import StateBlock from '@/components/StateBlock.vue'
import { ApiError } from '@/api/client'
import { useBreakpoints } from '@/composables/useBreakpoints'
import { useMenusStore } from '@/stores/menus'
import { useRestaurantsStore } from '@/stores/restaurants'
import type { RestaurantInput } from '@/api/restaurants'
import type { Restaurant } from '@/types'

const restaurants = useRestaurantsStore()
const menus = useMenusStore()
const router = useRouter()
const { isMobile } = useBreakpoints()

const formOpen = ref<'create' | string | null>(null)
const form = reactive<RestaurantInput>({ name: '', address: '', phone: '', email: '' })
const formError = ref<string | null>(null)
const formSubmitting = ref(false)
const deleteTarget = ref<Restaurant | null>(null)
const deleteError = ref<string | null>(null)

async function load(): Promise<void> {
  await restaurants.fetchMine()
  await Promise.all(restaurants.items.map((r) => menus.fetchSummaries(r.id)))
}

onMounted(load)

function menuLabel(restaurantId: string): string {
  const count = menus.summaries[restaurantId]?.length ?? 0
  return `${count} ${count === 1 ? 'menu' : 'menus'}`
}

function openCreate(): void {
  formOpen.value = 'create'
  formError.value = null
  Object.assign(form, { name: '', address: '', phone: '', email: '' })
}

function openEdit(restaurant: Restaurant): void {
  formOpen.value = restaurant.id
  formError.value = null
  Object.assign(form, {
    name: restaurant.name,
    address: restaurant.address,
    phone: restaurant.phone,
    email: restaurant.email,
  })
}

function cancelForm(): void {
  formOpen.value = null
  formError.value = null
}

async function submitForm(): Promise<void> {
  formError.value = null
  formSubmitting.value = true
  try {
    if (formOpen.value === 'create') {
      await restaurants.create({ ...form })
    } else if (formOpen.value) {
      await restaurants.update(formOpen.value, { ...form })
    }
    formOpen.value = null
  } catch (err) {
    formError.value = err instanceof ApiError ? err.message : 'Something went wrong.'
  } finally {
    formSubmitting.value = false
  }
}

function goToMenus(restaurantId: string): void {
  restaurants.setActive(restaurantId)
  router.push({ name: 'menu-builder', params: { restaurantId } })
}

async function confirmDelete(): Promise<void> {
  if (!deleteTarget.value) return
  deleteError.value = null
  try {
    await restaurants.remove(deleteTarget.value.id)
    deleteTarget.value = null
  } catch (err) {
    deleteError.value = err instanceof ApiError ? err.message : 'Something went wrong.'
  }
}

const emptyCopy = computed(() => ({
  title: 'No restaurants yet',
  body: 'A manager account starts empty. Creating the first restaurant injects the manager id server-side, so there is nothing to pick here.',
  action: 'Create a restaurant',
}))
</script>

<template>
  <div>
    <div style="display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end">
      <div style="margin-right: auto">
        <h6 style="color: var(--color-accent)">Manager</h6>
        <h2 style="margin: 0">My restaurants</h2>
      </div>
      <button class="btn btn-primary" type="button" style="flex: none; white-space: nowrap" @click="openCreate">
        New restaurant
      </button>
    </div>

    <div v-if="formOpen" class="manager-restaurants__form">
      <div class="field" style="margin-bottom: 10px">
        <label for="restaurant-name">Name</label>
        <input id="restaurant-name" class="input" type="text" v-model="form.name" />
      </div>
      <div class="field" style="margin-bottom: 10px">
        <label for="restaurant-address">Address</label>
        <input id="restaurant-address" class="input" type="text" v-model="form.address" />
      </div>
      <div class="field" style="margin-bottom: 10px">
        <label for="restaurant-phone">Phone</label>
        <input id="restaurant-phone" class="input" type="text" v-model="form.phone" />
      </div>
      <div class="field" style="margin-bottom: 10px">
        <label for="restaurant-email">Email</label>
        <input id="restaurant-email" class="input" type="email" v-model="form.email" />
      </div>
      <p v-if="formError" role="alert" style="font-size: 13px; color: var(--color-accent-700); margin: 0 0 10px">
        {{ formError }}
      </p>
      <div style="display: flex; gap: 8px">
        <button class="btn btn-primary" type="button" :disabled="formSubmitting" @click="submitForm">
          Save restaurant
        </button>
        <button class="btn btn-secondary" type="button" @click="cancelForm">Cancel</button>
      </div>
    </div>

    <div class="hr"></div>

    <StateBlock
      :status="restaurants.status"
      :error-message="restaurants.errorMessage"
      :empty-title="emptyCopy.title"
      :empty-body="emptyCopy.body"
      :empty-action-label="emptyCopy.action"
      @retry="load"
      @empty-action="openCreate"
    >
      <table v-if="!isMobile" class="table">
        <thead>
          <tr>
            <th scope="col">Name</th>
            <th scope="col">Address</th>
            <th scope="col">Phone</th>
            <th scope="col">Menus</th>
            <th scope="col"><span class="visually-hidden">Actions</span></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="restaurant in restaurants.items" :key="restaurant.id">
            <td style="font-weight: 800">{{ restaurant.name }}</td>
            <td class="text-muted">{{ restaurant.address }}</td>
            <td class="text-muted">{{ restaurant.phone }}</td>
            <td>{{ menuLabel(restaurant.id) }}</td>
            <td style="text-align: right; white-space: nowrap">
              <button class="btn btn-ghost" type="button" @click="goToMenus(restaurant.id)">Menus</button>
              <button class="btn btn-ghost" type="button" @click="openEdit(restaurant)">Edit</button>
              <button class="btn btn-ghost" type="button" @click="deleteTarget = restaurant">Delete</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-else style="display: flex; flex-direction: column; gap: 12px">
        <div v-for="restaurant in restaurants.items" :key="restaurant.id" class="card">
          <span class="card-kicker">{{ menuLabel(restaurant.id) }}</span>
          <span class="card-title">{{ restaurant.name }}</span>
          <p class="card-body">{{ restaurant.address }} · {{ restaurant.phone }}</p>
          <div style="display: flex; gap: 8px">
            <button class="btn btn-primary" type="button" style="min-height: 44px" @click="goToMenus(restaurant.id)">
              Menus
            </button>
            <button class="btn btn-secondary" type="button" style="min-height: 44px" @click="openEdit(restaurant)">
              Edit
            </button>
          </div>
        </div>
      </div>

      <p class="text-muted" style="font-size: 12px; margin-top: 16px">
        Names are unique per manager, not globally — the conflict error belongs on this form, not
        on a global check.
      </p>
    </StateBlock>

    <ConfirmDialog
      :open="deleteTarget !== null"
      title="Delete this restaurant?"
      :body="
        deleteError ?? `${deleteTarget?.name ?? ''} and its menus will no longer be reachable by customers.`
      "
      confirm-label="Delete permanently"
      cancel-label="Keep restaurant"
      @cancel="
        () => {
          deleteTarget = null
          deleteError = null
        }
      "
      @confirm="confirmDelete"
    />
  </div>
</template>

<style scoped>
.manager-restaurants__form {
  border: 1px solid var(--color-divider);
  padding: 14px;
  margin-top: 16px;
  max-width: 420px;
}

.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
}
</style>
