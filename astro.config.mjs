// @ts-check
import { defineConfig } from 'astro/config';
import tailwindcss from '@tailwindcss/vite';

// https://astro.build/config
export default defineConfig({
  // Dominio di produzione (Cloudflare Pages) — usato per canonical, Open Graph e sitemap.
  site: 'https://db3k-blog.pages.dev',
  trailingSlash: 'ignore',
  // Niente adapter: build statica + Cloudflare Pages Functions in ./functions
  // (deploy: npx wrangler pages deploy dist --project-name blog-db3k)
  vite: {
    plugins: [tailwindcss()],
    optimizeDeps: {
      exclude: ['blog-db3k/src/utils/media'],
    },
  },
});
