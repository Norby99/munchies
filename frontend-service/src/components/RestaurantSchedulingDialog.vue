<script setup lang="ts">
// Scheduling settings of one restaurant, as read by scheduler-service: the part of its capacity
// policy about time (opening hours, slots, lead time, kitchen units). Takeaway works without them,
// on the scheduler's default policy.
//
// The delivery part of the same policy (trips per slot, km per trip, detour), the restaurant's
// position and the delivery zones are edited on the Delivery page (ManagerDeliveryView). This
// dialog reads the whole policy and writes it back, so those fields are preserved untouched.
//
// The kitchen units per slot (maxCapacityUnits) are currently decided by the scheduler's admission
// quota agent, which overrides the value saved here; the field is kept because it is part of the
// policy contract and becomes the fallback when the agent has no answer.

import { reactive, ref, watch } from 'vue'
import { CircleAlertIcon } from '@lucide/vue'

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
import { Field, FieldDescription, FieldGroup, FieldLabel, FieldLegend, FieldSet } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { Spinner } from '@/components/ui/spinner'
import { ApiError } from '@/api/client'
import * as restaurantsApi from '@/api/restaurants'
import type { CapacityPolicy } from '@/api/restaurants'
import { SCHEDULER_DEFAULT_POLICY } from '@/utils/schedulerDefaults'
import type { Restaurant } from '@/types'

const props = defineProps<{ restaurant: Restaurant | null }>()
const emit = defineEmits<{ close: [] }>()

// Pre-filled with the scheduler's own default, so a first save changes nothing but enables
// deliveries.
const DEFAULT_POLICY: CapacityPolicy = { ...SCHEDULER_DEFAULT_POLICY }

const loading = ref(false)
const saving = ref(false)
const error = ref<string | null>(null)
const policy = reactive<CapacityPolicy>({ ...DEFAULT_POLICY })

watch(
  () => props.restaurant?.id,
  async (id) => {
    if (!id) return
    error.value = null
    loading.value = true
    Object.assign(policy, DEFAULT_POLICY)
    try {
      const savedPolicy = await restaurantsApi.getCapacityPolicy(id)
      if (savedPolicy) Object.assign(policy, savedPolicy)
    } catch (err) {
      error.value = err instanceof ApiError ? err.message : 'The current settings could not be loaded.'
    } finally {
      loading.value = false
    }
  },
  { immediate: true },
)

async function save(): Promise<void> {
  if (!props.restaurant) return
  error.value = null
  if (policy.opening >= policy.closing) {
    error.value = 'Opening time must be before closing time.'
    return
  }
  saving.value = true
  try {
    await restaurantsApi.updateCapacityPolicy(props.restaurant.id, { ...policy })
    emit('close')
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : 'The settings could not be saved.'
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
    <DialogContent class="max-h-[90dvh] overflow-y-auto sm:max-w-xl">
      <form class="contents" novalidate @submit.prevent="save">
        <DialogHeader>
          <DialogTitle>Scheduling for {{ restaurant?.name }}</DialogTitle>
          <DialogDescription>
            Customers book a time slot at checkout. The restaurant position, drivers and delivery zones are set on the
            Delivery page.
          </DialogDescription>
        </DialogHeader>

        <div v-if="loading" class="flex justify-center py-10"><Spinner class="size-6" /></div>

        <FieldGroup v-else>
          <FieldSet>
            <FieldLegend>Opening hours and slots</FieldLegend>
            <div class="grid gap-4 sm:grid-cols-3">
              <Field>
                <FieldLabel for="sched-opening">Opens at</FieldLabel>
                <Input id="sched-opening" v-model="policy.opening" class="h-10" type="time" required />
              </Field>
              <Field>
                <FieldLabel for="sched-closing">Closes at</FieldLabel>
                <Input id="sched-closing" v-model="policy.closing" class="h-10" type="time" required />
              </Field>
              <Field>
                <FieldLabel for="sched-slot">Slot length (min)</FieldLabel>
                <Input id="sched-slot" v-model.number="policy.slotMinutes" class="h-10" type="number" min="5" step="5" required />
              </Field>
              <Field>
                <FieldLabel for="sched-lead">Prep lead time (min)</FieldLabel>
                <Input id="sched-lead" v-model.number="policy.prepLeadMinutes" class="h-10" type="number" min="0" required />
              </Field>
              <Field>
                <FieldLabel for="sched-advance">Bookable days ahead</FieldLabel>
                <Input id="sched-advance" v-model.number="policy.maxAdvanceDays" class="h-10" type="number" min="1" required />
              </Field>
              <Field>
                <FieldLabel for="sched-units">Kitchen units per slot</FieldLabel>
                <Input id="sched-units" v-model.number="policy.maxCapacityUnits" class="h-10" type="number" min="1" required />
              </Field>
            </div>
            <FieldDescription>
              One unit is one item ordered. For now the scheduler's admission quota agent decides the units per slot and
              overrides this value.
            </FieldDescription>
          </FieldSet>

        </FieldGroup>

        <Alert v-if="error" variant="destructive" role="alert">
          <CircleAlertIcon />
          <AlertDescription>{{ error }}</AlertDescription>
        </Alert>

        <DialogFooter>
          <Button type="button" variant="outline" @click="emit('close')">Cancel</Button>
          <Button type="submit" :disabled="saving || loading">
            <Spinner v-if="saving" data-icon="inline-start" />
            Save settings
          </Button>
        </DialogFooter>
      </form>
    </DialogContent>
  </Dialog>
</template>
