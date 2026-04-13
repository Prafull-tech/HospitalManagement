import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const defaultAllowedHosts = mode === 'production'
    ? '.hms.com'
    : '.hms.local,localhost,127.0.0.1'
  const allowedHosts = (env.VITE_ALLOWED_HOSTS || defaultAllowedHosts)
    .split(',')
    .map((host) => host.trim())
    .filter(Boolean)

  return {
    plugins: [react()],
    server: {
      host: true,
      port: 3000,
      allowedHosts,
      watch: {
        usePolling: true,
      },
      proxy: {
        '/api': {
          // Use 127.0.0.1 to match the health-check scripts and avoid some Windows localhost/IPv6 quirks
          target: 'http://127.0.0.1:8080',
          changeOrigin: true,
          xfwd: true,
        },
        '/.well-known': {
          target: 'http://127.0.0.1:8080',
          changeOrigin: true,
          xfwd: true,
          rewrite: (path) => `/api${path}`,
        },
      },
    },
  }
})
