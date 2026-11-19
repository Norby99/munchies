<script setup lang="ts">
// Manager's Kitchen page for one restaurant: how the kitchen works, as its staff would describe
// it. It is the input of scheduler-service's capacity agent (analysis-scala), which will simulate
// this kitchen to choose how many items each time slot accepts.
// - How an item is made: the route (prepare then cook, like a pizza; or cook then prepare, like a
//   burger) and the mean minutes of each stage for one item, listed in the route's order.
// - Staff and stations: the people preparing items, and the cook stations with their places.
//
// The agent is not connected yet (the scheduler still uses a temporary random limit), and the page
// says so plainly: saving here changes nothing customers see today. Order sizes, demand and how
// much slower the kitchen really runs are not asked: the agent learns them from the order history.
//
// Until the kitchen is described, the form holds an example (a small pizzeria) and says it is one.

import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowLeftIcon,
  CircleAlertIcon,
  CircleCheckIcon,
  FlameIcon,
  HamburgerIcon,
  InfoIcon,
  PizzaIcon,
  PlusIcon,
  TrashIcon,
} from '@lucide/vue'

import ChoiceCard from '@/components/ChoiceCard.vue'
import PageHeader from '@/components/PageHeader.vue'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Field, FieldDescription, FieldGroup, FieldLabel, FieldLegend, FieldSet } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { RadioGroup } from '@/components/ui/radio-group'
import { Spinner } from '@/components/ui/spinner'
import { ApiError } from '@/api/client'
import * as restaurantsApi from '@/api/restaurants'
import type { KitchenRoute } from '@/api/restaurants'
import { useRestaurantsStore } from '@/stores/restaurants'
import {
  EXAMPLE_KITCHEN,
  formFrom,
  kitchenProblem,
  MAX_COOK_STATIONS,
  MAX_PREP_WORKERS,
  MAX_STAGE_MINUTES,
  MAX_STATION_NAME_LENGTH,
  MAX_STATION_PLACES,
  newStationKey,
  nextStationName,
  stagesOf,
  toWire,
  type KitchenForm,
  type Stage,
} from '@/utils/kitchenSetup'

const props = defineProps<{ restaurantId: string }>()

const router = useRouter()
const restaurants = useRestaurantsStore()

const restaurantName = ref('')
const loading = ref(true)
const loadError = ref<string | null>(null)
const saving = ref(false)
const error = ref<string | null>(null)
const saved = ref(false)
// True while the form shows the example rather than a saved description.
const isExample = ref(false)

const form = ref<KitchenForm>(formFrom(EXAMPLE_KITCHEN))

// Any edit hides the "saved" confirmation, so it never describes an older state.
watch(form, () => (saved.value = false), { deep: true })

// The time fields, in the order an item goes through them, so the form reads like the kitchen.
const STAGE_FIELDS: Record<Stage, { key: 'prepMinutes' | 'loadingMinutes' | 'cookMinutes'; label: string; hint: string }> = {
  prep: {
    key: 'prepMinutes',
    label: 'Prepare',
    hint: 'Hands-on work by one person: topping a pizza, building a burger.',
  },
  loading: {
    key: 'loadingMinutes',
    label: 'Load',
    hint: 'Putting the item into a station; the person is busy meanwhile. 0.5 is half a minute.',
  },
  cook: {
    key: 'cookMinutes',
    label: 'Cook',
    hint: 'Time in the oven or on the grill. Items cook side by side, one per place.',
  },
}
const stages = computed(() => stagesOf(form.value.route).map((stage) => STAGE_FIELDS[stage]))

async function load(): Promise<void> {
  loading.value = true
  loadError.value = null
  restaurants.setActive(props.restaurantId)
  try {
    const cached = restaurants.items.find((r) => r.id === props.restaurantId)
    const [restaurant, setup] = await Promise.all([
      cached ?? restaurantsApi.getRestaurant(props.restaurantId),
      restaurantsApi.getKitchenSetup(props.restaurantId),
    ])
    if (!cached) restaurants.cacheOne(restaurant)
    restaurantName.value = restaurant.name
    isExample.value = setup === null
    form.value = formFrom(setup ?? EXAMPLE_KITCHEN)
    await nextTick()
    saved.value = false
  } catch (err) {
    loadError.value = err instanceof ApiError ? err.message : 'The kitchen could not be loaded.'
  } finally {
    loading.value = false
  }
}

onMounted(load)
watch(() => props.restaurantId, load)

function setRoute(value: unknown): void {
  if (value === 'PREP_THEN_COOK' || value === 'COOK_THEN_PREP') form.value.route = value as KitchenRoute
}

function addStation(): void {
  form.value.stations.push({ key: newStationKey(), name: nextStationName(form.value.stations), places: 1 })
}

function removeStation(key: string): void {
  form.value.stations = form.value.stations.filter((s) => s.key !== key)
}

async function save(): Promise<void> {
  error.value = null
  saved.value = false
  const problem = kitchenProblem(form.value)
  if (problem) {
    error.value = problem
    return
  }
  saving.value = true
  try {
    const savedSetup = await restaurantsApi.updateKitchenSetup(props.restaurantId, toWire(form.value))
    // Keep the rows' keys (focus and order stay put); take the names as saved (trimmed).
    form.value.stations = form.value.stations.map((s, i) => ({ ...s, name: savedSetup.cookStations[i]?.name ?? s.name }))
    isExample.value = false
    // Let the "any edit hides the confirmation" watcher see the update above first.
    await nextTick()
    saved.value = true
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : 'The kitchen could not be saved.'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div>
    <Button variant="ghost" class="mb-4 -ml-2.5" @click="router.push({ name: 'manager-restaurants' })">
      <ArrowLeftIcon data-icon="inline-start" /> All restaurants
    </Button>
    <PageHeader
      eyebrow="Kitchen"
      :title="restaurantName || 'Kitchen'"
      description="How an item is made, who makes it and what it cooks in."
    >
      <template v-if="!loading && !loadError" #actions>
        <Button type="submit" form="kitchen-form" :disabled="saving">
          <Spinner v-if="saving" data-icon="inline-start" />
          Save kitchen
        </Button>
      </template>
    </PageHeader>

    <div v-if="loading" class="flex justify-center py-16"><Spinner class="size-6" /></div>

    <Alert v-else-if="loadError" variant="destructive" role="alert">
      <CircleAlertIcon />
      <AlertDescription class="flex flex-wrap items-center gap-3">
        {{ loadError }}
        <Button variant="outline" size="sm" @click="load">Try again</Button>
      </AlertDescription>
    </Alert>

    <form v-else id="kitchen-form" class="flex flex-col gap-6" novalidate @submit.prevent="save">
      <Alert>
        <InfoIcon />
        <AlertDescription>
          The capacity agent will use this description to decide how many items each time slot can accept. It is not
          active yet: for now a temporary limit applies, so saving here does not change what customers can order.
        </AlertDescription>
      </Alert>

      <!-- Outcome of the last save, announced to screen readers. -->
      <div aria-live="polite">
        <Alert v-if="error" variant="destructive">
          <CircleAlertIcon />
          <AlertDescription>{{ error }}</AlertDescription>
        </Alert>
        <Alert v-else-if="saved">
          <CircleCheckIcon class="text-success" />
          <AlertDescription>Kitchen saved.</AlertDescription>
        </Alert>
        <Alert v-else-if="isExample">
          <InfoIcon />
          <AlertDescription>
            Your kitchen is not described yet. The values below are an example, a small pizzeria: change them to match
            your kitchen, then save.
          </AlertDescription>
        </Alert>
      </div>

      <div class="grid items-start gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle class="text-base font-bold">How an item is made</CardTitle>
            <CardDescription>
              Pick the order most of your dishes follow, then the average minutes of each step for one item.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <FieldGroup class="gap-6">
              <FieldSet>
                <FieldLegend class="sr-only">Order of the steps</FieldLegend>
                <RadioGroup :model-value="form.route" class="grid gap-3" @update:model-value="setRoute">
                  <ChoiceCard
                    id="kitchen-route-prep"
                    value="PREP_THEN_COOK"
                    title="Prepare, then cook"
                    description="Like a pizza: topped, then baked"
                    :icon="PizzaIcon"
                  />
                  <ChoiceCard
                    id="kitchen-route-cook"
                    value="COOK_THEN_PREP"
                    title="Cook, then prepare"
                    description="Like a burger: grilled, then built"
                    :icon="HamburgerIcon"
                  />
                </RadioGroup>
              </FieldSet>

              <FieldSet>
                <FieldLegend variant="label">Minutes for one item, step by step</FieldLegend>
                <ol class="flex flex-col gap-4">
                  <li v-for="(stage, index) in stages" :key="stage.key">
                    <Field>
                      <FieldLabel :for="`kitchen-${stage.key}`">{{ index + 1 }}. {{ stage.label }}</FieldLabel>
                      <Input
                        :id="`kitchen-${stage.key}`"
                        v-model.number="form[stage.key]"
                        class="h-10"
                        type="number"
                        min="0.1"
                        :max="MAX_STAGE_MINUTES"
                        step="0.1"
                        required
                      />
                      <FieldDescription>{{ stage.hint }}</FieldDescription>
                    </Field>
                  </li>
                </ol>
              </FieldSet>
            </FieldGroup>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle class="text-base font-bold">Staff and stations</CardTitle>
            <CardDescription>Who prepares the items, and where they cook.</CardDescription>
          </CardHeader>
          <CardContent>
            <FieldGroup class="gap-6">
              <Field>
                <FieldLabel for="kitchen-workers">People preparing</FieldLabel>
                <Input
                  id="kitchen-workers"
                  v-model.number="form.prepWorkers"
                  class="h-10"
                  type="number"
                  min="1"
                  :max="MAX_PREP_WORKERS"
                  step="1"
                  required
                />
                <FieldDescription>Each prepares one item at a time, and also loads the stations.</FieldDescription>
              </Field>

              <FieldSet>
                <FieldLegend variant="label">Cook stations</FieldLegend>
                <FieldDescription>
                  Ovens, grills, fryers... Places are how many items cook in it at the same time.
                </FieldDescription>
                <ul class="flex flex-col gap-2" aria-label="Cook stations">
                  <li
                    v-for="(station, index) in form.stations"
                    :key="station.key"
                    class="flex items-center gap-2 rounded-lg border border-border p-2"
                  >
                    <FlameIcon class="size-4 shrink-0 text-muted-foreground" aria-hidden="true" />
                    <Input
                      v-model="station.name"
                      :aria-label="`Name of station ${index + 1}`"
                      :maxlength="MAX_STATION_NAME_LENGTH"
                      class="h-9 min-w-0 flex-1"
                    />
                    <Input
                      v-model.number="station.places"
                      :aria-label="`Places in ${station.name || `station ${index + 1}`}`"
                      class="h-9 w-20"
                      type="number"
                      min="1"
                      :max="MAX_STATION_PLACES"
                      step="1"
                      required
                    />
                    <span class="text-sm text-muted-foreground" aria-hidden="true">places</span>
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      :aria-label="`Remove station ${station.name || index + 1}`"
                      :disabled="form.stations.length === 1"
                      @click="removeStation(station.key)"
                    >
                      <TrashIcon />
                    </Button>
                  </li>
                </ul>
                <Button
                  type="button"
                  variant="outline"
                  class="self-start"
                  :disabled="form.stations.length >= MAX_COOK_STATIONS"
                  @click="addStation"
                >
                  <PlusIcon data-icon="inline-start" /> Add station
                </Button>
              </FieldSet>
            </FieldGroup>
          </CardContent>
        </Card>
      </div>
    </form>
  </div>
</template>
