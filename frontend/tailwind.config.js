/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      fontFamily: {
        display: ['"Playfair Display"', 'Georgia', 'serif'],
        body:    ['"IBM Plex Sans"', 'sans-serif'],
        mono:    ['"IBM Plex Mono"', 'monospace'],
      },
      colors: {
        forest:  { DEFAULT: '#1B3A2D', light: '#2A5A44', dark: '#0F2019' },
        terra:   { DEFAULT: '#C4622D', light: '#D97B46', dark: '#9E4E24' },
        cream:   { DEFAULT: '#FAFAF7', dark: '#F0F0EB' },
        ink:     { DEFAULT: '#1A1A1A', muted: '#4A4A4A' },
      },
    },
  },
  plugins: [],
};
