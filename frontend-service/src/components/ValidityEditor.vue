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
// Rows render as a single line — type select, a small inline label + input
// per field, remove button — rather than the stacked <label> above <input>
// layout that kept an earlier version two lines tall.
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
import { Plus, X } from 'lucide-vue-next'

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
  model.value = emitted.value
}

function flush(): void {
  if (pendingSyncTimer !== undefined) syncNow()
}

defineExpose({ flush })

onBeforeUnmount(flush)

function setMode(next: Mode): void {
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

function onWeeklyDaysInput(index: number, event: Event): void {
  const raw = (event.target as HTMLInputElement).value
  const days = raw
    .split(',')
    .map((part) => Number(part.trim()))
    .filter((n) => Number.isInteger(n) && n >= 1 && n <= 7)
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
  <div class="validity-editor">
    <h6 style="margin: 0 0 10px">Validity</h6>

    <div class="seg" style="margin-bottom: 12px">
      <label class="seg-opt">
        <input type="radio" name="validity-mode" :checked="mode === 'always'" @change="setMode('always')" />
        <span>Always</span>
      </label>
      <label class="seg-opt">
        <input type="radio" name="validity-mode" :checked="mode === 'custom'" @change="setMode('custom')" />
        <span>Custom</span>
      </label>
    </div>

    <template v-if="mode === 'custom'">
      <p v-if="rows.length === 0" class="text-muted" style="font-size: 13px; margin: 0 0 10px">
        No rules yet — add one below.
      </p>

      <div v-for="(rule, index) in rows" :key="index" class="validity-rule">
        <div class="validity-rule__row">
          <select
            class="input validity-rule__type"
            aria-label="Rule type"
            :value="rule.type"
            @change="changeType(index, ($event.target as HTMLSelectElement).value as CustomType)"
          >
            <option v-for="type in RULE_TYPES" :key="type" :value="type">{{ RULE_LABELS[type] }}</option>
          </select>

          <template v-if="rule.type === 'period'">
            <label class="validity-rule__field-label text-muted" :for="`validity-${index}-start`">Start</label>
            <input
              :id="`validity-${index}-start`"
              class="input"
              type="date"
              :value="rule.start"
              @change="patchRule(index, { start: ($event.target as HTMLInputElement).value })"
            />
            <span class="text-muted" style="flex: none">–</span>
            <label class="validity-rule__field-label text-muted" :for="`validity-${index}-end`">End</label>
            <input
              :id="`validity-${index}-end`"
              class="input"
              type="date"
              :value="rule.end"
              @change="patchRule(index, { end: ($event.target as HTMLInputElement).value })"
            />
          </template>

          <template v-else-if="rule.type === 'yearly'">
            <label class="validity-rule__field-label text-muted" :for="`validity-${index}-start`">Start</label>
            <input
              :id="`validity-${index}-start`"
              class="input validity-rule__narrow"
              type="text"
              placeholder="dd/mm"
              :value="formatDayMonth(rule.startDay, rule.startMonth)"
              @change="onYearlyInput(index, 'start', $event)"
            />
            <span class="text-muted" style="flex: none">–</span>
            <label class="validity-rule__field-label text-muted" :for="`validity-${index}-end`">End</label>
            <input
              :id="`validity-${index}-end`"
              class="input validity-rule__narrow"
              type="text"
              placeholder="dd/mm"
              :value="formatDayMonth(rule.endDay, rule.endMonth)"
              @change="onYearlyInput(index, 'end', $event)"
            />
          </template>

          <template v-else-if="rule.type === 'weekly'">
            <label class="validity-rule__field-label text-muted" :for="`validity-${index}-days`">Days</label>
            <input
              :id="`validity-${index}-days`"
              class="input"
              type="text"
              placeholder="1,2,3,4,5 (ISO, Mon–Sun)"
              :value="rule.days.join(',')"
              @change="onWeeklyDaysInput(index, $event)"
            />
          </template>

          <template v-else-if="rule.type === 'hours'">
            <label class="validity-rule__field-label text-muted" :for="`validity-${index}-start`">Start</label>
            <input
              :id="`validity-${index}-start`"
              class="input"
              type="time"
              :value="rule.start"
              @change="patchRule(index, { start: ($event.target as HTMLInputElement).value })"
            />
            <span class="text-muted" style="flex: none">–</span>
            <label class="validity-rule__field-label text-muted" :for="`validity-${index}-end`">End</label>
            <input
              :id="`validity-${index}-end`"
              class="input"
              type="time"
              :value="rule.end"
              @change="patchRule(index, { end: ($event.target as HTMLInputElement).value })"
            />
          </template>

          <button
            class="btn btn-ghost btn-icon"
            type="button"
            aria-label="Remove rule"
            style="margin-left: auto; flex: none"
            @click="removeRule(index)"
          >
            <X :size="16" />
          </button>
        </div>

        <p v-if="rule.type === 'period'" class="text-muted validity-rule__hint">
          Leave Start empty for "until", leave End empty for "from", fill both for a fixed range.
        </p>
      </div>

      <button class="btn btn-secondary" type="button" @click="addRule">
        <Plus :size="14" /> Add rule
      </button>
    </template>

    <p class="text-muted" style="font-size: 11px; margin: 12px 0 0">
      The server stores each rule but never evaluates it. "{{ describe(model) }}" is computed
      client-side from this list by <code>utils/validity.ts</code>.
    </p>
  </div>
</template>

<style scoped>
.validity-editor {
  border: 1px solid var(--color-divider);
  padding: 14px;
  margin-bottom: 20px;
}

.validity-rule {
  border: 1px solid var(--color-divider);
  padding: 8px 10px;
  margin-bottom: 8px;
}

.validity-rule__row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.validity-rule__row .input {
  width: auto;
  flex: 1 1 130px;
  min-width: 110px;
}

.validity-rule__type {
  flex: 0 1 130px;
  min-width: 110px;
}

.validity-rule__narrow {
  flex: 0 1 72px;
  min-width: 64px;
}

.validity-rule__field-label {
  font-size: 12px;
  flex: none;
  white-space: nowrap;
}

.validity-rule__hint {
  font-size: 11px;
  margin: 6px 0 0;
}
</style>
