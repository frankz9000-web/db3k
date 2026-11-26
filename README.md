# DB3K — blog personale (Astro + Tailwind CSS)

Blog statico minimalista: articoli in **Markdown**, stile con **Tailwind CSS v4**, homepage con la lista degli articoli e sistema di **backup delle revisioni**.

## Struttura del progetto

```
blog db3k/
├── src/
│   ├── content.config.ts       # Definizione della collection "posts"
│   ├── posts/                  # ✍️ I tuoi articoli (un file .md per post)
│   │   ├── benvenuto.md
│   │   └── setup-astro-tailwind.md
│   ├── layouts/Layout.astro    # Layout minimalista (header, footer, font)
│   ├── components/PostCard.astro# Card di un articolo nella homepage
│   ├── pages/
│   │   ├── index.astro         # Homepage: lista articoli in ordine cronologico
│   │   └── posts/[...slug].astro  # Pagina di ogni singolo articolo
│   └── styles/tailwind.css     # Tailwind + tipografia per il Markdown
├── backup/                     # 💾 Revisioni datate del blog (script sotto scripts/)
│   └── revisions/
├── scripts/
│   ├── backup.sh               # Backup su Linux/macOS
│   └── backup.ps1              # Backup su Windows
└── public/favicon.svg
```

## Comandi

```bash
npm install        # una volta sola
npm run dev        # sviluppo in locale → http://localhost:4321
npm run build      # genera lo statico finale in dist/
npm run preview    # anteprima della build
./scripts/backup.sh   # (oppure scripts/backup.ps1) salva una revisione in backup/
```

## Creare un nuovo articolo

Crea un file Markdown dentro `src/posts/`:

```md
---
title: "Titolo del post"
pubDate: 2026-11-03T09:00:00Z
description: "Una riga che appare nella homepage."
---

Contenuto in Markdown qui dentro...
```

Appare automaticamente nella homepage; la URL sarà `/posts/<nome-file>/`.

## Personalizzare

- **Nome del sito / nav**: `src/layouts/Layout.astro` (`siteName`, `<header>`)
- **Stili**: `src/styles/tailwind.css` (il resto con classi Tailwind nei file `.astro`)
- **Colore/branding favicon**: `public/favicon.svg`

## Notifiche push (app iOS)

### Testare / inviare notifiche senza scrivere un post

```bash
cd "C:/Users/frank/Desktop/blog db3k"
node send-push.js              # menu interattivo con template predefiniti
node send-push.js "Titolo" "Messaggio" [URL]  # invio diretto (non interattivo)
```

**Template disponibili nel menu:**
- 📢 Nuovo post sul blog
- 🏆 Risultati gara DB3K
- 🎉 Evento / uscita in programma
- Custom (titolo + messaggio personalizzati)
- Registra token di test manuale (per test senza l'app installata)

### Come funziona (architettura)

- **Endpoint live**: `https://db3k-blog.pages.dev/api/push/{register,unregister,list,send}` — Cloudflare **Pages Function** in `functions/api/push/[rest].ts` (parte della build statica, deployato con il sito).
- **Token**: salvati in Cloudflare **KV** (`PUSH_TOKENS`, id in `wrangler.jsonc` root), TTL 30 giorni (l'app re-registra ad ogni apertura).
- **Invio**: iOS → **Expo Push API** (`exp.host/--/api/v2/push/send`); Android → **FCM HTTP v1** (serve il secret `FCM_SERVICE_ACCOUNT_KEY_JSON` sul project Pages, già impostato).
- **App** (`ios-app/`, `android-app/`): registrano il token in `POST /api/push/register` e leggono il feed RSS.

### Setup (una volta sola — già fatto in questo project)

1. KV namespace `PUSH_TOKENS` creato e binding in `wrangler.jsonc` (root).
2. Secret FCM per Android: `npx wrangler pages secret put FCM_SERVICE_ACCOUNT_KEY_JSON --project-name blog-db3k` (già impostato).
3. Deploy: `npx astro build && npx wrangler pages deploy dist --project-name blog-db3k`.

### Test rapido (curl)

```bash
# registra un token di prova
curl -X POST https://db3k-blog.pages.dev/api/push/register \
  -H "Content-Type: application/json" \
  -d '{"token":"EXPO-TOKEN-APP-1-<device>","platform":"ios"}'

# quanti dispositivi sono registrati
curl https://db3k-blog.pages.dev/api/push/list

# invia una notifica a tutti
curl -X POST https://db3k-blog.pages.dev/api/push/send \
  -H "Content-Type: application/json" \
  -d '{"title":"DB3K","body":"Prova notifica"}'
```

> **Nota:** non serve un Worker separato: la Pages Function vive nel sito (`db3k-blog.pages.dev`) e usa lo stesso KV namespace.
