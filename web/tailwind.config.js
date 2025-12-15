/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        'primary': {
          DEFAULT: '#0D4F4F',
          light: '#1A6B6B',
          dark: '#083838',
        },
        'accent': {
          DEFAULT: '#D4AF37',
          light: '#E5C76B',
          dark: '#B8960C',
        },
        'background': '#0A0E17',
        'surface': '#141A27',
        'surface-light': '#1E2636',
        'card': '#1A2033',
        'text': {
          primary: '#F5F5F5',
          secondary: '#B0B8C9',
          muted: '#6B7280',
        },
        'success': '#10B981',
        'warning': '#F59E0B',
        'error': '#EF4444',
        'info': '#3B82F6',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      borderRadius: {
        'xl': '1rem',
        '2xl': '1.25rem',
        '3xl': '1.5rem',
      },
      boxShadow: {
        'glow': '0 0 20px rgba(13, 79, 79, 0.3)',
        'glow-accent': '0 0 20px rgba(212, 175, 55, 0.3)',
      },
    },
  },
  plugins: [],
}

