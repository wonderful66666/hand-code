import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
    proxy: {
      '/api/qbh': { target: 'http://127.0.0.1:8081' },
      '/api/ymy': { target: 'http://127.0.0.1:8082' },
      '/api/rust': { target: 'http://127.0.0.1:8083' },
    },
  },
})
