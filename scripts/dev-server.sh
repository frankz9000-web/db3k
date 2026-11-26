#!/usr/bin/env bash
# Gestione server dev Astro (porta 4321): verifica se gira, altrimenti lo avvia.
cd "$(dirname "$0")/.." || exit 1

if curl -s -o /dev/null --max-time 3 http://localhost:4321/; then
  echo "OK — server già attivo su http://localhost:4321/"
else
  nohup npm run dev >> astro-dev.log 2>&1 &
  sleep 8
  if curl -s -o /dev/null --max-time 3 http://localhost:4321/; then
    echo "OK — server avviato su http://localhost:4321/"
  else
    echo "ERRORE — avvio fallito, controlla astro-dev.log" >&2
    exit 1
  fi
fi
