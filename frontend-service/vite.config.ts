import { fileURLToPath, URL } from 'node:url'

import tailwindcss from '@tailwindcss/vite'
import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

export default defineConfig({
  // Tailwind CSS v4 (required by shadcn-vue): the plugin scans the sources for used classes.
  plugins: [vue(), tailwindcss()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        // Makes the SCSS abstracts (breakpoints, spacing, mixins) available in every component
        // without an explicit @use; they emit no CSS, so injecting them everywhere is free.
        additionalData: `@use "@/styles/abstracts" as *;\n`,
      },
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/users': 'http://localhost:8086',
      '/restaurants': 'http://localhost:8086',
      '/restaurant': 'http://localhost:8086',
      '/orders': 'http://localhost:8086',
      '/payments': 'http://localhost:8086',
    },
  },
})
