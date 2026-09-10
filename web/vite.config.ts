import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// Proxy the api-gateway services in dev so requests stay same-origin and avoid CORS.
// Target assumes `kubectl port-forward -n services svc/api-gateway 18080:8080` is running.
// An Ingress for ifood.local does exist, but with the podman rootless driver the minikube
// node IP isn't reachable from the host, so ifood.local (per .env.example) won't connect
// either -- the port-forward is the only working path until that driver limitation changes.
// Without it running, every request here 502s with Vite's default "Bad Gateway".
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
