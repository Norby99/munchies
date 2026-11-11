<script setup lang="ts">
// Title block shared by every page: optional eyebrow and description, actions on the right
// (wrapping below the title on narrow screens).
defineProps<{ title: string; eyebrow?: string; description?: string }>()
</script>

<template>
  <header class="mb-6 flex flex-wrap items-end justify-between gap-4 md:mb-8">
    <div class="flex min-w-0 flex-col gap-1">
      <p v-if="eyebrow" class="text-xs font-semibold tracking-wider text-primary uppercase">{{ eyebrow }}</p>
      <h1 class="text-2xl font-extrabold md:text-3xl">{{ title }}</h1>
      <p v-if="description || $slots.description" class="text-sm text-muted-foreground">
        <slot name="description">{{ description }}</slot>
      </p>
    </div>
    <div v-if="$slots.actions" class="flex flex-wrap items-center gap-2">
      <slot name="actions" />
    </div>
  </header>
</template>
