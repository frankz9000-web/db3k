// CHART lane: render the logged per-query hit/related matrix from research/db3k-presence-findings.json
// into a dark HTML chart and screenshot it to shots/db3k-presence-chart.png (puppeteer-core + local Chrome).
const fs=require('fs');
const path=require('path');
const ROOT=__dirname.replace(/[\\\/]/g,'/').replace(/\/scripts$/,'')+'/';
const FINDINGS='research/db3k-presence-findings.json';
const HTML_OUT=path.resolve(path.join(ROOT,FINDINGS),'../db3k-presence-chart.html');
const PNG_OUT=path.resolve('shots/db3k-presence-chart.png');

const d=JSON.parse(fs.readFileSync(path.join(ROOT,'research','db3k-presence-findings.json'),'utf8'));
const SWEEP=d.executed||'2026-11-04';
const rows=d.queries.map(q=>({n:q.n,lang:q.lang,total:(q.hitCount!=null?q.hitCount:(q.hits||[]).length),rel:(q.relatedHits!=null?q.relatedHits:(q.hits||[]).filter(h=>h.related).length),zero:!!q.zeroHit}));
const LABELS={1:'dominio db3k.netlify.app',2:"marchio \"DRIZ Bikers 3000\" (IT)",3:'tagline blog (IT)',4:'evento Urban Cross Caltan.',5:'post A "Corteo…" (IT)',6:'post B "Che domenica…" (IT)',7:'brand probe EN'};
const totalHits=rows.reduce((s,r)=>s+r.total,0);
const totalRel=rows.reduce((s,r)=>s+r.rel,0);
const zeroCount=rows.filter(r=>r.zero).length;
const LC=(d.summary&&d.summary.languagesCovered)?d.summary.languagesCovered:{IT:5,EN:1,'mixed/neutral proper nouns':1};

const W=1040,PX_PER_HIT=52,X0=240,Y0=76,ROW_H=52;
const CHART_BOT=Y0+rows.length*ROW_H,SVG_H=CHART_BOT+36;
let svg=`<svg width="${W}" height="${SVG_H}" viewBox="0 0 ${W} ${SVG_H}" role="img" aria-label="hit per query DB3K presence sweep">`;
// gridlines + axis ticks
for(let v=0;v<=10;v+=2){const x=X0+v*PX_PER_HIT;svg+=`<line x1="${x}" y1="${Y0-12}" x2="${x}" y2="${CHART_BOT}" stroke="#2b343a" stroke-width="1"/>`;svg+=`<text x="${x}" y="${CHART_BOT+20}" fill="#8a9aa6" font-size="13">${v}</text>`;}
// rows (reversed so Q7 top)
for(let i=0;i<rows.length;i++){const r=rows[rows.length-1-i];const y=Y0+i*ROW_H;
svg+=`<text x="${X0-16}" y="${y+24}" fill="#f4ede6" font-size="15" text-anchor="end">${r.n}. ${LABELS[r.n]}</text>`;
if(r.zero){svg+=`<rect x="${X0+8}" y="${y+14}" width="34" height="26" rx="6" fill="#d9a13a" opacity="0.25"/>`;svg+=`<text x="${X0+25}" y="${y+34}" font-size="17" text-anchor="middle">⌀</text>`;svg+=`<text x="${X0+64}" y="${y+32}" fill="#d9a13a" font-size="13">zero-hit — risultato nullo valido (logged)</text>`;}
else{svg+=`<rect x="${X0}" y="${y+12}" width="${r.total*PX_PER_HIT}" height="30" rx="7" fill="#48565e"/>`;svg+=`<rect x="${X0}" y="${y+12}" width="${Math.max(r.rel,8)*PX_PER_HIT}" height="30" rx="7" fill="#19c386"/>`;svg+=`<text x="${X0+r.total*PX_PER_HIT+10}" y="${y+34}" fill="#f4ede6" font-size="15">${r.rel}/${r.total}</text>`;}
}
svg+='</svg>';

const html=`<!doctype html><html lang="it"><head><meta charset="utf-8">
<title>DB3K — presence sweep chart</title>
<style>
body{background:#0d1012;color:#f4ede6;margin:0;padding:34px 40px;font-family:ui-sans-serif,system-ui,'Segoe UI',sans-serif}
h1{font-size:26px;letter-spacing:.5px;margin:0 0 6px}
.sub{color:#9fb2c0;font-size:15px;margin-bottom:18px}
.kpis{display:flex;gap:12px;margin-bottom:26px}
.kpi{flex:1;background:#161b1e;border:1px solid #232b2f;border-radius:10px;padding:12px 14px}
.kpi b{display:block;font-size:22px;color:#fff}.kpi span{font-size:12.5px;color:#9fb2c0}
.card{background:#161b1e;border:1px solid #232b2f;border-radius:12px;padding:18px 22px}
.legend{display:flex;gap:18px;font-size:13.5px;color:#c9d6df;margin-bottom:10px}
.lg i{display:inline-block;width:14px;height:14px;border-radius:4px;margin-right:7px;vertical-align:-1px}
.note{color:#8a9aa6;font-size:12.5px;margin-top:14px;line-height:1.5}
</style></head><body>
<h1>DB3K — presenza online · db3k.netlify.app</h1>
<div class="sub">Sweep ${SWEEP} · 7 query (≥6 richieste, EN+IT) · copertura linguaggi: IT ${LC.IT} + misto/neutro ${Object.keys(LC).filter(k=>k!=='IT'&&!/^EN/i.test(k)).length} + EN ${LC.EN} · fonte: research/db3k-presence-findings.json</div>
<div class="kpis">
<div class="kpi"><b>${rows.length}</b><span>query loggate (≥6 richieste)</span></div>
<div class="kpi"><b>${totalHits}</b><span>hit totali registrati</span></div>
<div class="kpi"><b style="color:#19c386">${totalRel}</b><span>hit rilevanti per l'entità DB3K</span></div>
<div class="kpi"><b>${zeroCount} di ${rows.length}</b><span>lane zero-hit = risultati nulli validi loggati</span></div>
<div class="kpi"><b>0</b><span>backlink a dominio registrati nello sweep</span></div>
</div>
<div class="card">
<div class="legend"><span><i style="background:#19c386"></i>rilevanti</span><span><i style="background:#48565e"></i>totali (sfondo)</span><span><i style="background:rgba(217,161,58,.4)">&nbsp;⌀&nbsp;</i>zero-hit valido</span></div>
${svg}
<div class="note">Asse X: conteggio hit per query (max 10). Q1/Q3/Q5/Q6 non restituiscono pagine che citino l'entità o il dominio — loggati come esiti nulli validi, non come fallimenti. Nessun risultato dello sweep linka a db3k.netlify.app.</div>
</div>
<script>window.__DATA__=${JSON.stringify({rows,totals:{totalHits,totalRel}})};void 0;</script>
</body></html>`;
fs.writeFileSync(HTML_OUT,html);
console.log('html -> '+HTML_OUT+' ('+Buffer.byteLength(html)+' bytes)');

const puppeteer=require('puppeteer-core');
(async()=>{
const cands=['C:\\Program Files\\Google Chrome\\Application\\chrome.exe','C:/Program Files/Google/Chrome/Application/chrome.exe'];
let ex=cands.find(p=>fs.existsSync(p));
if(!ex){throw new Error('chrome executable not found in: '+cands.join(' | '));}
const b=await puppeteer.launch({headless:'new',executablePath:ex});
const p=await b.newPage();
await p.setViewport({width:W+40,height:980,deviceScaleFactor:2});
// absolute file URL (Windows-safe)
const segs=HTML_OUT.replace(/\\/g,'/').split('/');
const fileUrl='file:///'+segs[0]+segs.slice(1).map(s=>'/'+encodeURIComponent(s)).join('');
await p.goto(fileUrl,{waitUntil:'load',timeout:60000});
await new Promise(r=>setTimeout(r,400));
await p.screenshot({path:PNG_OUT,fullPage:true});
console.log('png -> '+PNG_OUT+' ('+fs.statSync(PNG_OUT).size+' bytes)');
await b.close();
})().catch(e=>{console.error('ERR',e.message);process.exit(1)});
