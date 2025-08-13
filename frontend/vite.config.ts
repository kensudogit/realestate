import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    host: '0.0.0.0',  // リモートアクセスを許可
    port: 3000,
    strictPort: true,
    cors: true,  // CORSを有効化
    proxy: {
      '/api': {
        target: 'http://localhost:8082',  // バックエンドポートを8082に更新
        changeOrigin: true
      }
    }
  }
})
