# Salva una revisione datata degli articoli del blog in backup\revisions\.
$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '..')

$ts   = Get-Date -Format 'yyyy-MM-dd_HH-mm'
$dest = Join-Path 'backup/revisions' $ts

New-Item -ItemType Directory -Force -Path $dest | Out-Null
Copy-Item -Recurse -Force 'src/posts' (Join-Path $dest 'posts')
if (Test-Path 'src/content.config.ts') {
  Copy-Item -Force 'src/content.config.ts' $dest
}

Write-Host "Revisione salvata in $dest"
