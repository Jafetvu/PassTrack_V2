/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', 'sans-serif'],
      },
      colors: {
        brand: {
          primary: '#0284C7',
          ink: '#0F172A',
          muted: '#475569',
          page: '#F8FAFC',
          surface: '#FFFFFF',
          border: '#E2E8F0',
        },
      },
    },
  },
  plugins: [],
};
