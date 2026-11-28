import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('/@firebase/firestore/') && id.includes('/common-')) return 'firestore-common';
          if (id.includes('/@firebase/firestore/')) return 'firestore';
          if (id.includes('/@firebase/auth/')) return 'firebase-auth';
          if (id.includes('/@firebase/') || id.includes('/@protobufjs/') || id.includes('/protobufjs/')) return 'firebase-core';
          if (/\/node_modules\/(react|react-dom|scheduler)\//.test(id)) return 'react';
        }
      }
    }
  },
  server: {
    port: 3000,
    host: true
  },
  preview: {
    port: 3000,
    host: true
  }
});
