import { defineCollection, z } from 'astro:content';
import { glob } from 'astro/loaders';

// Ciascun file .md dentro src/posts/ è automaticamente un articolo.
const posts = defineCollection({
  loader: glob({ pattern: '**/*.md', base: './src/posts' }),
  schema: z.object({
    title: z.string(),
    pubDate: z.coerce.date(),
    description: z.string().optional(),
    updatedDate: z.coerce.date().optional(),
    tags: z.array(z.string()).optional(),
    image: z.string().optional(),
    /** Nome della cartella media in public/fb/ se diverso dall'id del post. */
    mediaDir: z.string().optional(),
  }),
});

// Ciascun file .md dentro src/content/pillole/ è una "pillola" (breve guida ciclistica).
const pillole = defineCollection({
  loader: glob({ pattern: '**/*.md', base: './src/content/pillole' }),
  schema: z.object({
    title: z.string(),
    pubDate: z.coerce.date(),
    description: z.string().optional(),
    tags: z.array(z.string()).optional(),
    /** Categoria: allenamento, nutrizione, attrezzatura, sicurezza, tecnica */
    category: z.enum(['allenamento', 'nutrizione', 'attrezzatura', 'sicurezza', 'tecnica']).optional(),
    /** Icona emoji (opzionale, es. 🍎 ⚡ 🚲 🔒 🎯) */
    emoji: z.string().optional(),
  }),
});

export const collections = { posts, pillole };
