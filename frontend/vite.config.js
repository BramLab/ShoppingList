// STANDARD (NO PROXY):
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
})

// (TEMP) FIX INSTEAD OF ABOVE, FOR error 400: Invalid CORS request:
// (Also adapted `.env` so the base URL is empty (requests stay on the same host):
// VITE_API_URL=

// WITH PROXY:
// import { defineConfig } from 'vite'
// import react from '@vitejs/plugin-react'
//
// export default defineConfig({
//   plugins: [react()],
//   server: {
//     proxy: {
//       '/api': 'http://localhost:8080',
//     },
//   },
// })
