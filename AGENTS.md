# Blog DB3K — note operative

## Avvio corretto del server locale

Il server NON crasha: l'avvio è lento (~9-10s) e i controlli prematuri fanno sembrare che sia morto.

Procedura corretta:

```bash
cd "C:/Users/frank/Desktop/blog db3k"
nohup npx astro dev --port 4321 > astro-dev.log 2>&1 & disown
sleep 15   # attendere l'avvio completo PRIMA di verificare
netstat -ano | findstr :4321   # deve mostrare LISTENING
curl -s -o /dev/null -w "%{http_code}" http://localhost:4321/   # deve restituire 200
```

Regole:
- Attendere almeno ~15s prima di verificare (npx + vite + sync content ≈ 10s).
- Verificare con `netstat`/`curl` DOPO l'attesa, non a 3-4s.
- Se compare "Another astro dev server is already running", la porta 4321 è già occupata: o il server è già attivo (usarlo) o lanciare `npx astro dev stop` prima di riprovare.
- Il log di riferimento è `astro-dev.log`; crash reali (stack trace) non si sono mai verificati — solo avvio lento e port-in-use.

## Build e deploy

```bash
cd "C:/Users/frank/Desktop/blog db3k"
npx astro build
npx wrangler pages deploy dist --project-name blog-db3k
```

- Sito live: https://db3k-blog.pages.dev
- I post vivono in `src/posts/*.md`; per nascondere un post dalla home basta spostarlo fuori da `src/posts/` (es. in `src/posts/.trash/`).
