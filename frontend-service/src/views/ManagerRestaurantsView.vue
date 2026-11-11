<script setup lang="ts">
// Manager: my restaurants, as cards with their menu count and quick links to orders and menus.
// Creating and editing happen in a dialog. Menu counts are not part of RestaurantDto, so they are
// derived by fetching each restaurant's menu summaries through the menus store.

import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  CircleAlertIcon,
  ClipboardListIcon,
  EllipsisVerticalIcon,
  MailIcon,
  MapPinIcon,
  PencilIcon,
  PhoneIcon,
  PlusIcon,
  SquareMenuIcon,
  StoreIcon,
  TrashIcon,
} from '@lucide/vue'

import ConfirmDialog from '@/components/ConfirmDialog.vue'
import PageHeader from '@/components/PageHeader.vue'
import StateBlock from '@/components/StateBlock.vue'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardAction, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Field, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { Spinner } from '@/components/ui/spinner'
import { ApiError } from '@/api/client'
import { useMenusStore } from '@/stores/menus'
import { useRestaurantsStore } from '@/stores/restaurants'
import type { RestaurantInput } from '@/api/restaurants'
import type { Restaurant } from '@/types'

const restaurants = useRestaurantsStore()
const menus = useMenusStore()
const router = useRouter()

// null: dialog closed; 'create': new restaurant; any other string: id of the restaurant edited.
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

function menuCount(restaurantId: string): number {
  return menus.summaries[restaurantId]?.length ?? 0
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

function onDialogOpenChange(open: boolean): void {
  if (!open) formOpen.value = null
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

function goToOrders(restaurantId: string): void {
  restaurants.setActive(restaurantId)
  router.push({ name: 'manager-orders', query: { restaurant: restaurantId } })
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

function cancelDelete(): void {
  deleteTarget.value = null
  deleteError.value = null
}
</script>

<template>
  <div>
    <PageHeader eyebrow="Manager" title="My restaurants" description="Manage your restaurants, their menus and their orders.">
      <template #actions>
        <Button @click="openCreate"><PlusIcon data-icon="inline-start" /> New restaurant</Button>
      </template>
    </PageHeader>

    <StateBlock
      :status="restaurants.status"
      :error-message="restaurants.errorMessage"
      empty-title="No restaurants yet"
      empty-body="Create your first restaurant, then add its menus to start receiving orders."
      :empty-icon="StoreIcon"
      empty-action-label="Create a restaurant"
      @retry="load"
      @empty-action="openCreate"
    >
      <ul class="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        <li v-for="restaurant in restaurants.items" :key="restaurant.id">
          <Card class="h-full">
            <CardHeader>
              <div class="flex items-center gap-3">
                <span class="grid size-10 shrink-0 place-items-center rounded-xl bg-accent text-accent-foreground">
                  <StoreIcon class="size-5" aria-hidden="true" />
                </span>
                <div class="min-w-0">
                  <CardTitle class="truncate text-lg font-bold">{{ restaurant.name }}</CardTitle>
                  <Badge variant="secondary" class="mt-1">
                    {{ menuCount(restaurant.id) }} {{ menuCount(restaurant.id) === 1 ? 'menu' : 'menus' }}
                  </Badge>
                </div>
              </div>
              <CardAction>
                <DropdownMenu>
                  <DropdownMenuTrigger as-child>
                    <Button variant="ghost" size="icon" :aria-label="`More actions for ${restaurant.name}`">
                      <EllipsisVerticalIcon />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end">
                    <DropdownMenuGroup>
                      <DropdownMenuItem @select="openEdit(restaurant)"><PencilIcon /> Edit details</DropdownMenuItem>
                    </DropdownMenuGroup>
                    <DropdownMenuSeparator />
                    <DropdownMenuGroup>
                      <DropdownMenuItem variant="destructive" @select="deleteTarget = restaurant">
                        <TrashIcon /> Delete
                      </DropdownMenuItem>
                    </DropdownMenuGroup>
                  </DropdownMenuContent>
                </DropdownMenu>
              </CardAction>
            </CardHeader>
            <CardContent class="flex-1">
              <ul class="flex flex-col gap-2 text-sm text-muted-foreground">
                <li class="flex items-center gap-2"><MapPinIcon class="size-4 shrink-0" aria-hidden="true" /><span class="truncate">{{ restaurant.address }}</span></li>
                <li class="flex items-center gap-2"><PhoneIcon class="size-4 shrink-0" aria-hidden="true" />{{ restaurant.phone }}</li>
                <li class="flex items-center gap-2"><MailIcon class="size-4 shrink-0" aria-hidden="true" /><span class="truncate">{{ restaurant.email }}</span></li>
              </ul>
            </CardContent>
            <CardFooter class="gap-2">
              <Button class="flex-1" @click="goToOrders(restaurant.id)"><ClipboardListIcon data-icon="inline-start" /> Orders</Button>
              <Button variant="outline" class="flex-1" @click="goToMenus(restaurant.id)"><SquareMenuIcon data-icon="inline-start" /> Menus</Button>
            </CardFooter>
          </Card>
        </li>
      </ul>
    </StateBlock>

    <Dialog :open="formOpen !== null" @update:open="onDialogOpenChange">
      <DialogContent class="sm:max-w-md">
        <form class="contents" @submit.prevent="submitForm">
          <DialogHeader>
            <DialogTitle>{{ formOpen === 'create' ? 'New restaurant' : 'Edit restaurant' }}</DialogTitle>
            <DialogDescription>Customers see these details on the restaurant page.</DialogDescription>
          </DialogHeader>
          <FieldGroup>
            <Field>
              <FieldLabel for="restaurant-name">Name</FieldLabel>
              <Input id="restaurant-name" v-model="form.name" class="h-10" type="text" required />
            </Field>
            <Field>
              <FieldLabel for="restaurant-address">Address</FieldLabel>
              <Input id="restaurant-address" v-model="form.address" class="h-10" type="text" autocomplete="street-address" required />
            </Field>
            <div class="grid gap-4 sm:grid-cols-2">
              <Field>
                <FieldLabel for="restaurant-phone">Phone</FieldLabel>
                <Input id="restaurant-phone" v-model="form.phone" class="h-10" type="tel" autocomplete="tel" required />
              </Field>
              <Field>
                <FieldLabel for="restaurant-email">Email</FieldLabel>
                <Input id="restaurant-email" v-model="form.email" class="h-10" type="email" autocomplete="email" required />
              </Field>
            </div>
            <Alert v-if="formError" variant="destructive" role="alert">
              <CircleAlertIcon />
              <AlertDescription>{{ formError }}</AlertDescription>
            </Alert>
          </FieldGroup>
          <DialogFooter>
            <Button type="button" variant="outline" @click="formOpen = null">Cancel</Button>
            <Button type="submit" :disabled="formSubmitting">
              <Spinner v-if="formSubmitting" data-icon="inline-start" />
              Save restaurant
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>

    <ConfirmDialog
      :open="deleteTarget !== null"
      title="Delete this restaurant?"
      :body="deleteError ?? `${deleteTarget?.name ?? ''} and its menus will no longer be reachable by customers.`"
      confirm-label="Delete permanently"
      cancel-label="Keep restaurant"
      @cancel="cancelDelete"
      @confirm="confirmDelete"
    />
  </div>
</template>
