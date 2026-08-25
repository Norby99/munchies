<script setup lang="ts">
import { formatEUR } from '@/utils/money'
import type { MenuItem } from '@/types'

const props = defineProps<{ item: MenuItem; disabled: boolean }>()
const emit = defineEmits<{ add: [] }>()
</script>

<template>
  <div class="menu-item-row">
    <div style="flex: 1">
      <div style="display: flex; align-items: baseline; gap: 10px">
        <strong style="font-size: 16px">{{ props.item.name }}</strong>
        <span v-if="props.item.variations.length" class="text-muted" style="font-size: 12px">
          {{ props.item.variations.map((v) => v.name).join(' · ') }}
        </span>
      </div>
      <p class="text-muted" style="font-size: 13px; margin: 4px 0 0; max-width: 52ch; text-wrap: pretty">
        {{ props.item.description }}
      </p>
    </div>
    <strong style="font-variant-numeric: tabular-nums; font-size: 16px; flex: none; white-space: nowrap">
      {{ formatEUR(props.item.price) }}
    </strong>
    <button
      class="btn btn-secondary"
      type="button"
      :disabled="props.disabled"
      style="min-height: 44px; min-width: 44px; flex: none; white-space: nowrap"
      @click="emit('add')"
    >
      Add
    </button>
  </div>
</template>

<style scoped>
.menu-item-row {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  padding: 14px 0;
  border-bottom: 1px solid var(--color-divider);
}
</style>
