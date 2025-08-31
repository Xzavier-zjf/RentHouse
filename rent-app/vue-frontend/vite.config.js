import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000
  },
  optimizeDeps: {
    include: ['vue', 'vue-router', 'axios', 'element-plus']
  },
  build: {
    rollupOptions: {
      input: {
        main: './index.html'
      }
    }
  }
})