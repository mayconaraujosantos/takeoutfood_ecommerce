import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// Proxy the api-gateway services in dev so requests stay same-origin and avoid CORS.
// Target assumes `kubectl port-forward -n services svc/api-gateway 8090:8080` is running
// (no Ingress resource exists yet for ifood.local, so we bypass it locally).
// Port 8090 avoids clashing with the argocd-server port-forward, which typically uses 8080.
const services = ['auth-service', 'restaurant-service', 'menu-service', 'order-service']

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: Object.fromEntries(
      services.map((service) => [
        `/${service}`,
        { target: 'http://localhost:8090', changeOrigin: true },
      ]),
    ),
  },
})
