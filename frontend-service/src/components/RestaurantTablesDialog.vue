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
import type { DiningTable, Restaurant } from '@/types'

const props = defineProps<{ restaurant: Restaurant | null }>()
const emit = defineEmits<{ close: [] }>()

// The layout is edited as groups, "how many tables x seats each" (e.g. 2 x 8 and 4 x 4), which is
// how staff describe a dining room. The service stores individual tables, so on save the groups
// are expanded in order and numbered 1..N; on load, consecutive tables with the same seats are
// folded back into one group. An earlier version asked for each table's number, and a manager
// read that column as a count: numbering automatically removes that ambiguity.
//
// Values are kept as strings while editing: an empty field must stay empty, not become 0.
interface Group {
  key: number
  count: string
  seats: string
}

const groups = ref<Group[]>([])
const loading = ref(false)
const saving = ref(false)
const error = ref<string | null>(null)
let nextKey = 0

const totalTables = computed(() => groups.value.reduce((sum, g) => sum + (Number(g.count) || 0), 0))
const totalSeats = computed(() =>
  groups.value.reduce((sum, g) => sum + (Number(g.count) || 0) * (Number(g.seats) || 0), 0),
)

// The table numbers each group will get, e.g. "Tables 3-6", shown so staff can label the room.
const numbering = computed(() => {
  let next = 1
  return groups.value.map((g) => {
    const count = Number(g.count)
    if (!Number.isInteger(count) || count < 1) return ''
    const first = next
    next += count
    return count === 1 ? `Table ${first}` : `Tables ${first}-${next - 1}`
  })
})

function foldIntoGroups(tables: DiningTable[]): Group[] {
  const folded: Group[] = []
  for (const table of tables.slice().sort((a, b) => a.number - b.number)) {
    const last = folded[folded.length - 1]
    if (last && Number(last.seats) === table.seats) last.count = String(Number(last.count) + 1)
    else folded.push({ key: nextKey++, count: '1', seats: String(table.seats) })
  }
  return folded
}

watch(
  () => props.restaurant?.id,
  async (id) => {
    if (!id) return
    error.value = null
    groups.value = []
    loading.value = true
    try {
      groups.value = foldIntoGroups(await restaurantsApi.getTables(id))
    } catch (err) {
      error.value = err instanceof ApiError ? err.message : 'The tables could not be loaded.'
    } finally {
      loading.value = false
    }
  },
  { immediate: true },
)

function addGroup(): void {
  groups.value.push({ key: nextKey++, count: '1', seats: '4' })
}

function removeGroup(key: number): void {
  groups.value = groups.value.filter((g) => g.key !== key)
}

function validate(): string | null {
  for (const group of groups.value) {
    const count = Number(group.count)
    const seats = Number(group.seats)
    if (!Number.isInteger(count) || count < 1) return 'Every row needs at least one table.'
    if (!Number.isInteger(seats) || seats < 1) return 'Every table needs at least one seat.'
  }
  return null
}

function expand(): DiningTable[] {
  let number = 1
  return groups.value.flatMap((g) =>
    Array.from({ length: Number(g.count) }, () => ({ number: number++, seats: Number(g.seats) })),
  )
}

async function save(): Promise<void> {
  if (!props.restaurant) return
  error.value = validate()
  if (error.value) return
  saving.value = true
  try {
    await restaurantsApi.updateTables(props.restaurant.id, expand())
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
            Customers can book once you list your tables, e.g. 2 tables of 8 seats and 4 of 4. Each party gets the
            smallest free table that fits, and tables are never joined: the largest table is the largest party.
          </DialogDescription>
        </DialogHeader>

        <div v-if="loading" class="flex justify-center py-10"><Spinner class="size-6" /></div>

        <template v-else>
          <p v-if="groups.length === 0" class="rounded-xl border border-dashed p-6 text-center text-sm text-muted-foreground">
            No tables yet: this restaurant does not take reservations.
          </p>
          <table v-else class="w-full text-sm">
            <caption class="sr-only">Tables of the dining room, grouped by size</caption>
            <thead class="text-left text-muted-foreground">
              <tr>
                <th scope="col" class="pb-2 font-medium">How many tables</th>
                <th scope="col" class="pb-2 font-medium">Seats each</th>
                <th scope="col" class="pb-2"><span class="sr-only">Remove</span></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(group, index) in groups" :key="group.key">
                <td class="py-1 pr-3 align-top">
                  <Input
                    v-model="group.count"
                    class="h-9"
                    type="number"
                    min="1"
                    step="1"
                    :aria-label="`How many tables in row ${index + 1}`"
                    :aria-describedby="`tables-numbering-${group.key}`"
                  />
                  <span :id="`tables-numbering-${group.key}`" class="mt-1 block text-xs text-muted-foreground">
                    {{ numbering[index] }}
                  </span>
                </td>
                <td class="py-1 pr-3 align-top">
                  <Input v-model="group.seats" class="h-9" type="number" min="1" step="1" :aria-label="`Seats at each table in row ${index + 1}`" />
                </td>
                <td class="py-1 text-right align-top">
                  <Button type="button" variant="ghost" size="icon-sm" :aria-label="`Remove row ${index + 1}`" @click="removeGroup(group.key)">
                    <Trash2Icon />
                  </Button>
                </td>
              </tr>
            </tbody>
          </table>
          <div class="flex items-center justify-between gap-3">
            <Button type="button" variant="outline" size="sm" @click="addGroup">
              <PlusIcon data-icon="inline-start" /> Add tables
            </Button>
            <span v-if="groups.length" class="text-sm text-muted-foreground tabular-nums" role="status">
              {{ totalTables }} {{ totalTables === 1 ? 'table' : 'tables' }}, {{ totalSeats }} seats
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
