# Backup delle revisioni del blog

Qui finiscono gli **snapshot datati** degli articoli. Ogni snapshot è una copia completa e autonoma di `src/posts/` (più la configurazione della collection), pronta per essere confrontata o ripristinata.

## Struttura

```
backup/
└── revisions/
    ├── 2026-11-02_23-05/      ← una cartella per ogni snapshot
    │   ├── posts/             ─ copia di src/posts/ in quel momento
    │   └── content.config.ts
```

## Come fare un backup

Dal **root** del progetto:

| Sistema | Comando |
|---|---|
| Linux / macOS (bash) | `./scripts/backup.sh` |
| Windows (PowerShell) | `powershell -File scripts/backup.ps1` |

Non c'è nessun limite al numero di revisioni: i vecchi restano intatti.

## Ripristinare una revisione

Copia il contenuto della revisione su `src/posts/`:

```bash
cp backup/revisions/<data>/posts/* src/posts/
```

## Consigli

- Fai un backup **prima** di riscrivere a fondo un post o il tema (layout/Tailwind).
- I file dentro le snapshot sono solo Markdown + config: puoi aprirli e diffarli con qualsiasi editor.
- `backup/revisions` è ignorato da Git (vedi `.gitignore`) perché è una copia; se vuoi versionarla comunque, toglila dal gitignore.
