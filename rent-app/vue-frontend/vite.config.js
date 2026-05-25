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
      },
      output: {
        manualChunks(id) {
          if (id.includes('node_modules')) {
            if (id.includes('node_modules/vue') || id.includes('node_modules/vue-router') || id.includes('node_modules/vuex')) {
              return 'vue'
            }
            if (id.includes('node_modules/axios')) {
              return 'axios'
            }
            if (id.includes('node_modules/@element-plus/icons-vue')) {
              return 'element-icons'
            }
            if (id.includes('node_modules/element-plus/es/components')) {
              const componentName = id.split('node_modules/element-plus/es/components/')[1]?.split('/')[0]
              if (['table', 'table-column', 'date-picker', 'select', 'option', 'dialog', 'descriptions', 'descriptions-item'].includes(componentName)) {
                return 'element-data'
              }
              if (['form', 'form-item', 'input', 'input-number', 'button', 'row', 'col'].includes(componentName)) {
                return 'element-form'
              }
              return `element-${componentName?.charAt(0) || 'misc'}`
            }
            if (id.includes('node_modules/element-plus')) {
              return 'element-core'
            }
            if (id.includes('node_modules/@popperjs') || id.includes('node_modules/@floating-ui')) {
              return 'floating'
            }
            return 'vendor'
          }
        }
      }
    }
  }
})
