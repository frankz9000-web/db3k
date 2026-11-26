# Backup DB3K — Blog
Crea: 17/11/2026
Progetto: C:/Users/frank/Desktop/blog db3k

## Contenuto del backup
Tutto il progetto **tranne**:
- `node_modules/` (605 MB) → rigenerabile con `npm install`
- `dist/` (214 MB) → rigenerabile con `npx astro build`
- `.git/` (2 GB) → la storia completa è già sul remote GitHub `https://github.com/dongbeater3000/db3k`
- `.astro/`, `__pycache__/` → artefatti di build

## Cosa c'è dentro
| Cartella | Contenuto |
|---|---|
| `src/` | Codice sorgente Astro: layout, pagine, componenti, **tutti i post** (src/posts/*.md) |
| `public/` | **Tutto il media** (213 MB): foto, video, album FB, favicon, og-default.png, file verifica Google |
| `functions/` | Cloudflare Pages Function `/api/hit` (contatore visitatori) |
| `scripts/` | Script di utility |
| `data/`, `fb/`, `fb_photos/`, `fbcx/`, `shots/`, `research/` | Working data: dump Facebook, estrazioni, screenshot |
| `backup/`, `blog-online-static/` | Backup precedenti e copia statica del sito online |
| `*.json`, `*.html`, `*.py` a radice | File di lavoro estrazione/import Facebook |
| Config | `astro.config.mjs`, `package.json`, `package-lock.json`, `tsconfig.json`, `wrangler.jsonc`, `README.md` |

## Come ripristinare
1. Estrai l'archivio in una cartella (es. `C:\Users\frank\Desktop\blog db3k\`)
2. `npm install`
3. `npx astro dev` (sviluppo) oppure:
   - `npx astro build` + deploy Cloudflare: `npx wrangler pages deploy dist --project-name=blog-db3k` (i token CF sono nel `.env` del progetto, letto automaticamente da Wrangler)

## Sito live
- https://db3k-blog.pages.dev (Cloudflare Pages, progetto `blog-db3k`)
- Google Search Console: verificato con file `public/google913435ac80649f55.html` (lasciarlo in `public/`!)
