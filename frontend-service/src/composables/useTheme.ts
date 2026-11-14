// Light / dark / system color theme. The choice is stored per browser under the same key that the
// inline script in index.html reads before the first paint, and is applied as the `dark` class on
// <html>, which switches the theme tokens defined in assets/index.css.

import { useColorMode } from '@vueuse/core'

export type ThemeChoice = 'light' | 'dark' | 'auto'

export function useTheme() {
  const mode = useColorMode({
    storageKey: 'munchies-color-scheme',
    disableTransition: true,
  })

  return {
    // The stored choice ('auto' follows the operating system setting).
    choice: mode.store,
  }
}
