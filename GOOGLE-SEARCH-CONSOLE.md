# Indicizzazione Blog DB3K su Google — Guida operativa

## 1. Creare la proprietà in Google Search Console

1. Vai su **https://search.google.com/search-console**
2. Clicca **"Aggiungi una proprietà"** (in alto a sinistra)
3. Scegli **"Prefisso URL"** (non "Proprietà di dominio" — più semplice per Pages)
4. Inserisci: `https://db3k-blog.pages.dev`
5. Clicca **Continua**

## 2. Verificare la proprietà

Google ti chiederà di dimostrare che il sito è tuo. Hai due opzioni:

### Opzione A — Meta tag (consigliata, già presente)
- Il file `src/layouts/Layout.astro` contiene già questo tag:
  ```html
  <meta name="google-site-verification" content="VsU77y5C8dy-R3XWL7IRA0dGRpRiGq7QdNge5xQmStI">
  ```
- Clicca **"Conferma tramite meta tag"** → copia il valore e incollalo nel campo di verifica.

### Opzione B — File HTML (alternativa)
- Scarica il file `.html` fornito da Google
- Caricalo in `public/` del progetto Astro
- Fai un commit + deploy su Cloudflare Pages
- Clicca "Verifica"

## 3. Inviare la sitemap

1. Nella dashboard della proprietà, nel menu a sinistra clicca **"Sitemap"** (sotto "Indice")
2. Inserisci: `sitemap.xml`
3. Clicca **Invia**

Dovresti vedere subito:
- URL inviati: **68**
- URL indicizzati: **0** (inizialmente)

## 4. Richiedere l'indicizzazione delle pagine chiave

Per ogni pagina importante (home, pillole, albums):

1. Vai su **"URL e pagine"** → **"Esplora come Google"** (o "Ispezione URL")
2. Incolla l'URL, es: `https://db3k-blog.pages.dev/`
3. Clicca **"Richiedi indicizzazione"** → **"Conferma"**

Fallo per le pagine principali:
- `https://db3k-blog.pages.dev/` (home)
- `https://db3k-blog.pages.dev/pillole/`
- `https://db3k-blog.pages.dev/albums/`
- `https://db3k-blog.pages.dev/reels/`

## 5. Monitorare i progressi

Dopo qualche giorno:
- **Indice** → "Pagine" → vedi quante pagine sono indicizzate
- **Prestazioni** → vedi query e click da Google Search
- **Copertura** → eventuali errori di crawling

## ⏱️ Tempi stimati

| Azione | Tempo |
|--------|-------|
| Creazione proprietà + verifica | 5 min |
| Invio sitemap | 1 min |
| Prima indicizzazione (sitemap) | 2-7 giorni |
| Indicizzazione singola (richiesta manuale) | 1-3 giorni |

## 📌 Note importanti

- **Cloudflare Pages** è un hosting valido e Google lo crawlizza normalmente.
- Il blog ha già tutti i tag SEO: canonical, OG, Twitter Cards, JSON-LD.
- La sitemap include anche le pagine dei tag (35+ URL) — utile per la scoperta di contenuti correlati.
- Se dopo 2 settimane non vedi indicizzazione, controlla che il file `robots.txt` sia accessibile da `https://db3k-blog.pages.dev/robots.txt`.
