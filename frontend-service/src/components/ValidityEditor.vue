<script setup lang="ts">
// Menu availability rule editor. The server stores whatever rules are sent
// but never evaluates them — the "Available now" / "Closed now" tag
// elsewhere is always computed client-side from the same rules via
// utils/validity.ts.
//
// A menu's validity is a list of rules (AND via list cardinality — see
// utils/validity.ts), but this editor only offers a single rule at a time:
// always, weekly, hours, or yearly. period/from/until exist in the domain but
// aren't offered here, matching the design's segmented control exactly.

import { computed } from 'vue'

import { describe } from '@/utils/validity'
import type { Validity } from '@/types'

const model = defineModel<Validity[]>({ required: true })

type EditorType = 'always' | 'weekly' | 'hours' | 'yearly'

const EDITOR_TYPES: EditorType[] = ['always', 'weekly', 'hours', 'yearly']

const PRESETS: Record<EditorType, Validity> = {
  always: { type: 'always' },
  weekly: { type: 'weekly', days: [1, 2, 3, 4, 5] },
  hours: { type: 'hours', start: '18:00', end: '23:30' },
  yearly: { type: 'yearly', startMonth: 12, startDay: 1, endMonth: 1, endDay: 6 },
}

const rule = computed<Validity>(() => model.value[0] ?? PRESETS.always)

const activeType = computed<EditorType>(() => {
  const type = rule.value.type
  return (EDITOR_TYPES as string[]).includes(type) ? (type as EditorType) : 'always'
})

function selectType(type: EditorType): void {
  model.value = [PRESETS[type]]
}

interface FlatFieldsPatch {
  start?: string
  end?: string
  days?: number[]
  startDay?: number
  startMonth?: number
  endDay?: number
  endMonth?: number
}

function updateRule(patch: FlatFieldsPatch): void {
  model.value = [{ ...rule.value, ...patch } as Validity]
}

function onWeeklyDaysInput(event: Event): void {
  const raw = (event.target as HTMLInputElement).value
  const days = raw
    .split(',')
    .map((part) => Number(part.trim()))
    .filter((n) => Number.isInteger(n) && n >= 1 && n <= 7)
  updateRule({ days })
}

function onYearlyInput(field: 'start' | 'end', event: Event): void {
  const raw = (event.target as HTMLInputElement).value
  const [day, month] = raw.split('/').map((part) => Number(part.trim()))
  if (!day || !month) return
  if (field === 'start') updateRule({ startDay: day, startMonth: month })
  else updateRule({ endDay: day, endMonth: month })
}
</script>

<template>
  <div class="validity-editor">
    <h6 style="margin: 0 0 10px">Validity</h6>

    <div class="seg" style="flex-wrap: wrap; margin-bottom: 12px">
      <label v-for="type in EDITOR_TYPES" :key="type" class="seg-opt">
        <input
          type="radio"
          name="validity-type"
          :checked="activeType === type"
          @change="selectType(type)"
        />
        <span>{{ type }}</span>
      </label>
    </div>

    <div style="display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end">
      <template v-if="rule.type === 'hours'">
        <div class="field" style="min-width: 150px">
          <label for="validity-start">Start</label>
          <input
            id="validity-start"
            class="input"
            type="time"
            :value="rule.start"
            @change="updateRule({ start: ($event.target as HTMLInputElement).value })"
          />
        </div>
        <div class="field" style="min-width: 150px">
          <label for="validity-end">End</label>
          <input
            id="validity-end"
            class="input"
            type="time"
            :value="rule.end"
            @change="updateRule({ end: ($event.target as HTMLInputElement).value })"
          />
        </div>
      </template>

      <template v-else-if="rule.type === 'weekly'">
        <div class="field" style="min-width: 200px">
          <label for="validity-days">Days (ISO 1–7)</label>
          <input
            id="validity-days"
            class="input"
            type="text"
            :value="rule.days.join(',')"
            @change="onWeeklyDaysInput"
          />
        </div>
      </template>

      <template v-else-if="rule.type === 'yearly'">
        <div class="field" style="min-width: 150px">
          <label for="validity-yearly-start">Start (d/m)</label>
          <input
            id="validity-yearly-start"
            class="input"
            type="text"
            :value="`${rule.startDay}/${rule.startMonth}`"
            @change="onYearlyInput('start', $event)"
          />
        </div>
        <div class="field" style="min-width: 150px">
          <label for="validity-yearly-end">End (d/m)</label>
          <input
            id="validity-yearly-end"
            class="input"
            type="text"
            :value="`${rule.endDay}/${rule.endMonth}`"
            @change="onYearlyInput('end', $event)"
          />
        </div>
      </template>
    </div>

    <p class="text-muted" style="font-size: 11px; margin: 12px 0 0">
      The server stores the rule but never evaluates it. "{{ describe(model) }}" is computed
      client-side from this rule by <code>utils/validity.ts</code>.
    </p>
  </div>
</template>

<style scoped>
.validity-editor {
  border: 1px solid var(--color-divider);
  padding: 14px;
  margin-bottom: 20px;
}
</style>
