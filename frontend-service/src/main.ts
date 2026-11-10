import { createPinia } from 'pinia'
import { createApp } from 'vue'

import App from '@/App.vue'
import { router } from '@/router'

// Tailwind and the shadcn-vue theme first. The legacy styles.css is unlayered, so it still wins
// over Tailwind's layered base styles while it is being migrated (see CLAUDE.md, Styling).
import '@/assets/index.css'
import '@/assets/styles.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
