<script setup lang="ts">
// Menu availability rule editor. The server stores whatever rules are sent
// but never evaluates them — the "Available now" / "Closed now" tag
// elsewhere is always computed client-side from the same rules via
// utils/validity.ts.
//
// Two top-level modes: Always (no restriction — emits [{type:'always'}]) or
// Custom, which reveals a rule list. Each row is one of 4 kinds — Date range,
// Yearly, Weekly, Hours — all AND together, matching the wire format's
// list-cardinality-is-AND exactly (see utils/validity.ts). Custom-mode rows
// are kept in a local `rows` ref independent of `model` so toggling back to
// Always and then back to Custom doesn't lose whatever was staged — only
// actually leaving/reloading this menu does (see MenuBuilderView.vue's :key
// on this component).
//
// The wire format has no combined "date range" type — PeriodValidity (both
// ends), FromValidity (start only) and UntilValidity (end only) are three
// separate types. Rather than exposing that split in the UI, a single "Date
// range" row holds start/end locally as a period-shaped rule and toEmitted()
// downgrades it to FROM/UNTIL when only one side is filled, or drops it
// entirely (emits nothing) when both are empty.
//
// Every row starts empty — PRESETS holds no real defaults, just the type's
// shape with blank/sentinel field values (empty string for text fields, 0
// for Yearly's day/month, since 0 is never a valid day or month) — and
// inputs show their format as a placeholder instead. toEmitted() treats an
// incomplete row (Hours/Yearly/Weekly need every field; Date range accepts
// one side, see above) as contributing nothing, so addRule() never talks to
// the backend by itself. Any field commit debounces 3s before syncing (see
// patchRule below), so filling in a row's fields collapses into one save
// instead of one PUT per field.

import { computed, onBeforeUnmount, ref } from 'vue'
import { CalendarClockIcon, PlusIcon, XIcon } from '@lucide/vue'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { NativeSelect, NativeSelectOption } from '@/components/ui/native-select'
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group'

import { describe } from '@/utils/validity'
import type { Validity } from '@/types'

const model = defineModel<Validity[]>({ required: true })

type Mode = 'always' | 'custom'
type CustomType = 'period' | 'yearly' | 'weekly' | 'hours'

const RULE_TYPES: CustomType[] = ['period', 'yearly', 'weekly', 'hours']

const RULE_LABELS: Record<CustomType, string> = {
  period: 'Date range',
  yearly: 'Yearly',
  weekly: 'Weekly',
  hours: 'Hours',
}

const PRESETS: Record<CustomType, Validity> = {
  period: { type: 'period', start: '', end: '' },
  yearly: { type: 'yearly', startMonth: 0, startDay: 0, endMonth: 0, endDay: 0 },
  weekly: { type: 'weekly', days: [] },
  hours: { type: 'hours', start: '', end: '' },
}

function formatDayMonth(day: number, month: number): string {
  return day !== 0 && month !== 0 ? `${day}/${month}` : ''
}

function toRow(rule: Validity): Validity {
  if (rule.type === 'from') return { type: 'period', start: rule.start, end: '' }
  if (rule.type === 'until') return { type: 'period', start: '', end: rule.end }
  return rule
}

function toEmitted(rule: Validity): Validity | null {
  switch (rule.type) {
    case 'period': {
      const hasStart = rule.start !== ''
      const hasEnd = rule.end !== ''
      if (hasStart && hasEnd) return rule
      if (hasStart) return { type: 'from', start: rule.start }
      if (hasEnd) return { type: 'until', end: rule.end }
      return null
    }
    case 'hours':
      return rule.start !== '' && rule.end !== '' ? rule : null
    case 'weekly':
      return rule.days.length > 0 ? rule : null
    case 'yearly':
      return rule.startDay !== 0 && rule.startMonth !== 0 && rule.endDay !== 0 && rule.endMonth !== 0 ? rule : null
    default:
      return rule
  }
}

const initialRows = model.value.filter((r) => r.type !== 'always').map(toRow)
const mode = ref<Mode>(initialRows.length > 0 ? 'custom' : 'always')
const rows = ref<Validity[]>(initialRows)

const emitted = computed<Validity[]>(() => rows.value.map(toEmitted).filter((r): r is Validity => r !== null))

let pendingSyncTimer: ReturnType<typeof setTimeout> | undefined

function syncNow(): void {
  clearTimeout(pendingSyncTimer)
  pendingSyncTimer = undefined
  // No complete rule means no restriction: send "always" rather than an empty list, which
  // restaurant-service rejects. Skip the update entirely when nothing would change (e.g. switching
  // to Custom before any rule is filled in), so no needless PUT reaches the backend.
  const next: Validity[] = emitted.value.length > 0 ? emitted.value : [{ type: 'always' }]
  if (JSON.stringify(next) !== JSON.stringify(model.value)) model.value = next
}

function flush(): void {
  if (pendingSyncTimer !== undefined) syncNow()
}

defineExpose({ flush })

onBeforeUnmount(flush)

function setMode(next: unknown): void {
  // The toggle group emits an empty value when the active item is clicked again: ignore it.
  if (next !== 'always' && next !== 'custom') return
  mode.value = next
  if (next === 'always') {
    clearTimeout(pendingSyncTimer)
    pendingSyncTimer = undefined
    model.value = [{ type: 'always' }]
  } else {
    syncNow()
  }
}

function addRule(): void {
  rows.value = [...rows.value, PRESETS.period]
}

function removeRule(index: number): void {
  rows.value = rows.value.filter((_, i) => i !== index)
  syncNow()
}

function changeType(index: number, type: CustomType): void {
  rows.value = rows.value.map((rule, i) => (i === index ? PRESETS[type] : rule))
  syncNow()
}

function patchRule(index: number, patch: Partial<Validity>): void {
  rows.value = rows.value.map((rule, i) => (i === index ? ({ ...rule, ...patch } as Validity) : rule))
  clearTimeout(pendingSyncTimer)
  pendingSyncTimer = setTimeout(syncNow, 3000)
}

const WEEKDAYS = [
  { day: 1, short: 'Mon', long: 'Monday' },
  { day: 2, short: 'Tue', long: 'Tuesday' },
  { day: 3, short: 'Wed', long: 'Wednesday' },
  { day: 4, short: 'Thu', long: 'Thursday' },
  { day: 5, short: 'Fri', long: 'Friday' },
  { day: 6, short: 'Sat', long: 'Saturday' },
  { day: 7, short: 'Sun', long: 'Sunday' },
]

// Weekly rule days (ISO numbers, Monday = 1) picked with a multi-select toggle group.
function onWeeklyDays(index: number, values: unknown): void {
  const days = (Array.isArray(values) ? values : [])
    .map((value) => Number(value))
    .filter((n) => Number.isInteger(n) && n >= 1 && n <= 7)
    .sort((a, b) => a - b)
  patchRule(index, { days })
}

function onYearlyInput(index: number, field: 'start' | 'end', event: Event): void {
  const raw = (event.target as HTMLInputElement).value.trim()
  if (raw === '') {
    patchRule(index, field === 'start' ? { startDay: 0, startMonth: 0 } : { endDay: 0, endMonth: 0 })
    return
  }
  const [day, month] = raw.split('/').map((part) => Number(part.trim()))
  if (!day || !month) return
  patchRule(index, field === 'start' ? { startDay: day, startMonth: month } : { endDay: day, endMonth: month })
}
</script>

<template>
  <section aria-labelledby="validity-title" class="flex flex-col gap-3 rounded-xl border bg-muted/40 p-4">
    <div class="flex flex-wrap items-center gap-3">
      <h3 id="validity-title" class="flex items-center gap-2 text-base font-bold">
        <CalendarClockIcon class="size-4 text-muted-foreground" aria-hidden="true" /> Availability
      </h3>
      <ToggleGroup
        type="single"
        variant="outline"
        :model-value="mode"
        class="ml-auto bg-card"
        aria-label="Availability mode"
        @update:model-value="setMode"
      >
        <ToggleGroupItem value="always" class="px-3">Always</ToggleGroupItem>
        <ToggleGroupItem value="custom" class="px-3">Custom</ToggleGroupItem>
      </ToggleGroup>
    </div>

    <p class="text-sm text-muted-foreground">
      {{ mode === 'always' ? 'Customers can order from this menu at any time.' : `Currently: ${describe(model)}.` }}
    </p>

    <template v-if="mode === 'custom'">
      <p v-if="rows.length === 0" class="text-sm text-muted-foreground">
        No rules yet. Add one: all rules must hold for the menu to be open.
      </p>

      <div v-for="(rule, index) in rows" :key="index" class="flex flex-col gap-2 rounded-xl border bg-card p-3">
        <div class="flex flex-wrap items-center gap-2">
          <NativeSelect
            :model-value="rule.type"
            class="w-36"
            :aria-label="`Rule ${index + 1} type`"
            @update:model-value="(value: unknown) => changeType(index, value as CustomType)"
          >
            <NativeSelectOption v-for="type in RULE_TYPES" :key="type" :value="type">{{ RULE_LABELS[type] }}</NativeSelectOption>
          </NativeSelect>

          <template v-if="rule.type === 'period' || rule.type === 'hours'">
            <label class="sr-only" :for="`validity-${index}-start`">From</label>
            <Input
              :id="`validity-${index}-start`"
              class="h-8 w-auto min-w-32 flex-1"
              :type="rule.type === 'period' ? 'date' : 'time'"
              :model-value="rule.start"
              @change="patchRule(index, { start: ($event.target as HTMLInputElement).value })"
            />
            <span class="text-sm text-muted-foreground" aria-hidden="true">to</span>
            <label class="sr-only" :for="`validity-${index}-end`">To</label>
            <Input
              :id="`validity-${index}-end`"
              class="h-8 w-auto min-w-32 flex-1"
              :type="rule.type === 'period' ? 'date' : 'time'"
              :model-value="rule.end"
              @change="patchRule(index, { end: ($event.target as HTMLInputElement).value })"
            />
          </template>

          <template v-else-if="rule.type === 'yearly'">
            <label class="sr-only" :for="`validity-${index}-start`">From (day/month)</label>
            <Input
              :id="`validity-${index}-start`"
              class="h-8 w-24"
              type="text"
              placeholder="dd/mm"
              :model-value="formatDayMonth(rule.startDay, rule.startMonth)"
              @change="onYearlyInput(index, 'start', $event)"
            />
            <span class="text-sm text-muted-foreground" aria-hidden="true">to</span>
            <label class="sr-only" :for="`validity-${index}-end`">To (day/month)</label>
            <Input
              :id="`validity-${index}-end`"
              class="h-8 w-24"
              type="text"
              placeholder="dd/mm"
              :model-value="formatDayMonth(rule.endDay, rule.endMonth)"
              @change="onYearlyInput(index, 'end', $event)"
            />
          </template>

          <ToggleGroup
            v-else-if="rule.type === 'weekly'"
            type="multiple"
            variant="outline"
            size="sm"
            :model-value="rule.days.map(String)"
            class="flex-wrap"
            :aria-label="`Rule ${index + 1} days of the week`"
            @update:model-value="(values) => onWeeklyDays(index, values)"
          >
            <ToggleGroupItem v-for="weekday in WEEKDAYS" :key="weekday.day" :value="String(weekday.day)" :aria-label="weekday.long">
              {{ weekday.short }}
            </ToggleGroupItem>
          </ToggleGroup>

          <Button type="button" variant="ghost" size="icon-sm" class="ml-auto" :aria-label="`Remove rule ${index + 1}`" @click="removeRule(index)">
            <XIcon />
          </Button>
        </div>

        <p v-if="rule.type === 'period'" class="text-xs text-muted-foreground">
          Leave one side empty for an open-ended range.
        </p>
      </div>

      <Button type="button" variant="outline" size="sm" class="self-start bg-card" @click="addRule">
        <PlusIcon data-icon="inline-start" /> Add rule
      </Button>
    </template>
  </section>
</template>
