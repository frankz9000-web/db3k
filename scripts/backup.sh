#!/usr/bin/env bash
# Salva una revisione datata degli articoli del blog in backup/revisions/.
set -euo pipefail
cd "$(dirname "$0")/.."

ts="$(date +%Y-%m-%d_%H-%M)"
dest="backup/revisions/$ts"

mkdir -p "$dest"
cp -r src/posts "$dest/"
cp src/content.config.ts "$dest/content.config.ts" 2>/dev/null || true

# Nota: se il progetto è un repo git, aggiungi qui `git log -1` per tracciare la commit.
echo "✅ Revisione salvata in $dest"
