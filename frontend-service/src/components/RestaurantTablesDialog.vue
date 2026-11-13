<script setup lang="ts">
// The dining room of one restaurant: its tables, each with a number and seats. table-reservation-
// service seats every party at the smallest free table that fits, so this layout is what makes a
// restaurant bookable; with no tables, customers are not offered "Book a table".
//
// The layout is saved as a whole (PUT replaces it). Like RestaurantSchedulingDialog, this dialog
// reads and saves its own settings, so the restaurant list stays free of per-feature state.

import { computed, ref, watch } from 'vue'
import { CircleAlertIcon, PlusIcon, Trash2Icon } from '@lucide/vue'

import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Spinner } from '@/components/ui/spinner'
import { ApiError } from '@/api/client'
import * as restaurantsApi from '@/api/restaurants'
import type { Restaurant } from '@/types'

const props = defineProps<{ restaurant: Restaurant | null }>()
const emit = defineEmits<{ close: [] }>()

// Kept as strings while editing: an empty field must stay empty, not become 0.
interface Row {
  key: number
  number: string
  seats: string
}

const rows = ref<Row[]>([])
const loading = ref(false)
const saving = ref(false)
const error = ref<string | null>(null)
let nextKey = 0

const totalSeats = computed(() => rows.value.reduce((sum, r) => sum + (Number(r.seats) || 0), 0))

watch(
  () => props.restaurant?.id,
  async (id) => {
    if (!id) return
    error.value = null
    rows.value = []
    loading.value = true
    try {
      const tables = await restaurantsApi.getTables(id)
      rows.value = tables
        .slice()
        .sort((a, b) => a.number - b.number)
        .map((t) => ({ key: nextKey++, number: String(t.number), seats: String(t.seats) }))
    } catch (err) {
      error.value = err instanceof ApiError ? err.message : 'The tables could not be loaded.'
    } finally {
      loading.value = false
    }
  },
  { immediate: true },
)

function addTable(): void {
  const highest = rows.value.reduce((max, r) => Math.max(max, Number(r.number) || 0), 0)
  rows.value.push({ key: nextKey++, number: String(highest + 1), seats: '4' })
}

function removeTable(key: number): void {
  rows.value = rows.value.filter((r) => r.key !== key)
}

// Same rules as restaurant-service: positive whole numbers, table numbers unique.
function validate(): string | null {
  const numbers = new Set<number>()
  for (const row of rows.value) {
    const number = Number(row.number)
    const seats = Number(row.seats)
    if (!Number.isInteger(number) || number < 1) return 'Every table needs a number of 1 or more.'
    if (!Number.isInteger(seats) || seats < 1) return `Table ${row.number} needs at least one seat.`
    if (numbers.has(number)) return `Table number ${number} is used twice.`
    numbers.add(number)
  }
  return null
}

async function save(): Promise<void> {
  if (!props.restaurant) return
  error.value = validate()
  if (error.value) return
  saving.value = true
  try {
    await restaurantsApi.updateTables(
      props.restaurant.id,
      rows.value.map((r) => ({ number: Number(r.number), seats: Number(r.seats) })),
    )
    emit('close')
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : 'The tables could not be saved.'
  } finally {
    saving.value = false
  }
}

function onOpenChange(open: boolean): void {
  if (!open) emit('close')
}
</script>

<template>
  <Dialog :open="restaurant !== null" @update:open="onOpenChange">
    <DialogContent class="max-h-[90dvh] overflow-y-auto sm:max-w-md">
      <form class="contents" novalidate @submit.prevent="save">
        <DialogHeader>
          <DialogTitle>Tables at {{ restaurant?.name }}</DialogTitle>
          <DialogDescription>
            Customers can book a table once you list them here. Each party gets the smallest free table that fits.
          </DialogDescription>
        </DialogHeader>

        <div v-if="loading" class="flex justify-center py-10"><Spinner class="size-6" /></div>

        <template v-else>
          <p v-if="rows.length === 0" class="rounded-xl border border-dashed p-6 text-center text-sm text-muted-foreground">
            No tables yet: this restaurant does not take reservations.
          </p>
          <table v-else class="w-full text-sm">
            <caption class="sr-only">Tables of the dining room</caption>
            <thead class="text-left text-muted-foreground">
              <tr>
                <th scope="col" class="pb-2 font-medium">Table number</th>
                <th scope="col" class="pb-2 font-medium">Seats</th>
                <th scope="col" class="pb-2"><span class="sr-only">Remove</span></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in rows" :key="row.key">
                <td class="py-1 pr-3">
                  <Input v-model="row.number" class="h-9" type="number" min="1" step="1" :aria-label="`Number of table ${row.number}`" />
                </td>
                <td class="py-1 pr-3">
                  <Input v-model="row.seats" class="h-9" type="number" min="1" step="1" :aria-label="`Seats at table ${row.number}`" />
                </td>
                <td class="py-1 text-right">
                  <Button type="button" variant="ghost" size="icon-sm" :aria-label="`Remove table ${row.number}`" @click="removeTable(row.key)">
                    <Trash2Icon />
                  </Button>
                </td>
              </tr>
            </tbody>
          </table>
          <div class="flex items-center justify-between gap-3">
            <Button type="button" variant="outline" size="sm" @click="addTable">
              <PlusIcon data-icon="inline-start" /> Add a table
            </Button>
            <span v-if="rows.length" class="text-sm text-muted-foreground tabular-nums">
              {{ rows.length }} {{ rows.length === 1 ? 'table' : 'tables' }}, {{ totalSeats }} seats
            </span>
          </div>
        </template>

        <Alert v-if="error" variant="destructive" role="alert">
          <CircleAlertIcon />
          <AlertDescription>{{ error }}</AlertDescription>
        </Alert>

        <DialogFooter>
          <Button type="button" variant="outline" @click="emit('close')">Cancel</Button>
          <Button type="submit" :disabled="saving || loading">
            <Spinner v-if="saving" data-icon="inline-start" />
            Save tables
          </Button>
        </DialogFooter>
      </form>
    </DialogContent>
  </Dialog>
</template>
