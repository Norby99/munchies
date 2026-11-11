<script setup lang="ts">
// Kitchen load of one day, from scheduler-service: for every slot, the kitchen units already
// booked against the slot's admission quota, plus the delivery trips planned in it. Read-only;
// it refreshes when the restaurant or the day changes, or when the parent bumps `refreshKey`
// (e.g. after the board's Refresh).
//
// On today's view, slots that started more than an hour ago are hidden unless they hold bookings:
// the manager cares about what is still coming.

import { computed, ref, watch } from 'vue'
import { BikeIcon, CircleAlertIcon } from '@lucide/vue'

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Field, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { Spinner } from '@/components/ui/spinner'
import { ApiError } from '@/api/client'
import * as scheduleApi from '@/api/schedule'
import type { KitchenLoad, LoadSlot } from '@/api/schedule'
import { toDateInput } from '@/utils/time'

const props = defineProps<{ restaurantId: string; refreshKey?: number }>()

const today = toDateInput(new Date())
const date = ref(today)
const load = ref<KitchenLoad | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

const visibleSlots = computed<LoadSlot[]>(() => {
  const slots = load.value?.slots ?? []
  if (date.value !== today) return slots
  const cutoff = Date.now() - 60 * 60_000
  return slots.filter((s) => s.startEpochMs >= cutoff || s.bookedUnits > 0)
})

const bookedToday = computed(() => load.value?.totalBookedUnits ?? 0)

async function fetchLoad(): Promise<void> {
  if (!props.restaurantId || !date.value) return
  loading.value = true
  error.value = null
  try {
    load.value = await scheduleApi.getKitchenLoad(props.restaurantId, date.value)
  } catch (err) {
    load.value = null
    error.value = err instanceof ApiError ? err.message : 'The kitchen load could not be loaded.'
  } finally {
    loading.value = false
  }
}

watch(() => [props.restaurantId, date.value, props.refreshKey], fetchLoad, { immediate: true })

function ratio(slot: LoadSlot): number {
  return slot.capacityUnits > 0 ? Math.min(1, slot.bookedUnits / slot.capacityUnits) : 0
}

// Full: no unit left. Busy: 80% or more of the quota used.
function fillClass(slot: LoadSlot): string {
  if (slot.remainingUnits === 0) return 'bg-destructive'
  if (ratio(slot) >= 0.8) return 'bg-warning'
  return 'bg-primary'
}
</script>

<template>
  <Card size="sm" class="mb-6">
    <CardHeader class="flex flex-wrap items-end justify-between gap-3">
      <div>
        <CardTitle class="text-base">Kitchen load</CardTitle>
        <CardDescription>
          Units booked per slot against the admission quota<template v-if="load">
            &middot; {{ bookedToday }} booked on this day</template
          >
        </CardDescription>
      </div>
      <Field orientation="horizontal" class="w-auto gap-2">
        <FieldLabel for="kitchen-load-date" class="text-sm">Day</FieldLabel>
        <Input id="kitchen-load-date" v-model="date" type="date" class="h-9 w-40" />
      </Field>
    </CardHeader>
    <CardContent>
      <div v-if="loading && !load" class="flex justify-center py-6"><Spinner class="size-5" /></div>
      <p v-else-if="error" role="alert" class="flex items-center gap-2 text-sm text-destructive">
        <CircleAlertIcon class="size-4 shrink-0" aria-hidden="true" /> {{ error }}
      </p>
      <p v-else-if="visibleSlots.length === 0" class="py-4 text-center text-sm text-muted-foreground">
        No more slots on this day.
      </p>
      <ul v-else class="flex gap-2 overflow-x-auto pb-2" aria-label="Slots of the day">
        <li
          v-for="slot in visibleSlots"
          :key="slot.startEpochMs"
          class="flex w-24 shrink-0 flex-col gap-1.5 rounded-xl border bg-card p-2.5 text-xs"
        >
          <span class="font-semibold tabular-nums">{{ slot.start }}</span>
          <div
            class="h-1.5 overflow-hidden rounded-full bg-muted"
            role="meter"
            :aria-label="`Kitchen units booked at ${slot.start}`"
            :aria-valuenow="slot.bookedUnits"
            aria-valuemin="0"
            :aria-valuemax="slot.capacityUnits"
          >
            <div :class="['h-full rounded-full', fillClass(slot)]" :style="{ width: `${ratio(slot) * 100}%` }" />
          </div>
          <span class="tabular-nums text-muted-foreground">
            {{ slot.bookedUnits }}/{{ slot.capacityUnits }} units
          </span>
          <span v-if="slot.tripCount > 0" class="flex items-center gap-1 text-muted-foreground">
            <BikeIcon class="size-3" aria-hidden="true" /> {{ slot.tripCount }} {{ slot.tripCount === 1 ? 'trip' : 'trips' }}
          </span>
        </li>
      </ul>
    </CardContent>
  </Card>
</template>
