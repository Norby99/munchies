// Two real breakpoints, both driven by window width, backed by a single
// shared resize listener (module-level state) rather than one per component.
//
// < 768px is treated as the mobile chrome switch (bottom tab bar, tighter
// header/main padding) — the design doc leaves this threshold implicit since
// its prototype toggled a fixed 390px frame instead of resizing; 768px is the
// conventional phone/tablet cutoff and keeps the mobile chrome aligned with
// the <1150px grid-collapse breakpoint that IS specified.
// < 1150px collapses the builder/detail grids to one column.
// >= 1440px widens gaps and middle-column padding.

import { computed, onScopeDispose, ref } from 'vue'

const width = ref(typeof window === 'undefined' ? 1440 : window.innerWidth)
let listeners = 0

function handleResize(): void {
  width.value = window.innerWidth
}

export function useBreakpoints() {
  if (typeof window !== 'undefined') {
    if (listeners === 0) window.addEventListener('resize', handleResize)
    listeners += 1

    onScopeDispose(() => {
      listeners -= 1
      if (listeners === 0) window.removeEventListener('resize', handleResize)
    })
  }

  const isMobile = computed(() => width.value < 768)
  const isNarrow = computed(() => width.value < 1150)
  const isWide = computed(() => width.value >= 1440)

  return { width, isMobile, isNarrow, isWide }
}
