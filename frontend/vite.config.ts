import react from '@vitejs/plugin-react';
import {defineConfig} from 'vite';

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [
        react(),
    ],
    build: {
        chunkSizeWarningLimit: 1000,
        rollupOptions: {
            output: {
                manualChunks(id) {
                    if (id.includes('node_modules')) {
                        if (id.includes('@mui')) {
                            return 'vendor-ui';
                        }
                        if (id.includes('react')) {
                            return 'vendor-react';
                        }
                        return 'vendor';
                    }
                },
            },
        },
    },
})