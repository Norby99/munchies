import { fileURLToPath, URL } from 'node:url'

import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/users': 'http://localhost:8086',
      '/restaurants': 'http://localhost:8086',
      '/restaurant': 'http://localhost:8086',
      '/orders': 'http://localhost:8086',
    },
  },
})
