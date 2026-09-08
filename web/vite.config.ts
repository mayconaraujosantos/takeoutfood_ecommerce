import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// Proxy the api-gateway services in dev so requests stay same-origin and avoid CORS.
// Target assumes `kubectl port-forward -n services svc/api-gateway 18080:8080` is running
// (no Ingress resource exists yet for ifood.local, so we bypass it locally).
const services = ['auth-service', 'restaurant-service', 'menu-service', 'order-service']

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: Object.fromEntries(
      services.map((service) => [
        `/${service}`,
        { target: 'http://localhost:18080', changeOrigin: true },
      ]),
    ),
  },
})
