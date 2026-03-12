/** @type {import('tailwindcss').Config} */
export default {
    content: ["./index.html", "./src/**/*.{js,jsx}"],
    theme: {
        extend: {
            fontFamily: {
                display: ['"Lora"', 'Georgia', 'serif'],
                body:    ['"DM Sans"', 'sans-serif'],
                mono:    ['"DM Mono"', 'monospace'],
            },
            colors: {
                kale:      { DEFAULT: '#1C3D2E', light: '#2d5c42', dark: '#0f2218' },
                leaf:      { DEFAULT: '#4a7c59', light: '#5d9970' },
                sprout:    { DEFAULT: '#8fbf96', light: '#b8d9bc' },
                mist:      { DEFAULT: '#e8f3ea', dark: '#d4e8d7' },
                parchment: { DEFAULT: '#F7F5F0', dark: '#F0EDE5' },
                carrot:    { DEFAULT: '#E8732A', light: '#F5A05A', dark: '#c95e1e' },
                soil:      { DEFAULT: '#5C3D1E', light: '#7a5230' },
                ink:       { DEFAULT: '#1a2e1e', muted: '#5a7a62' },
                sage:      { DEFAULT: '#ddeadf', dark: '#c5d9c8' },
            },
        },
    },
    plugins: [],
};
