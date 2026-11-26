const fs = require("fs");
let s = fs.readFileSync("src/pages/index.astro", "utf8");
const L = s.split("\n");
const i = L.findIndex(x => x.indexOf("{t}") !== -1 && x.indexOf("<a") !== -1);
if (i < 0) process.exit(42);
L[i] = '        <a href={`/tabs/${t}`} class=\'rounded-full border border-white/10 bg-carbon px-3 py-1.5 text-xs font-medium text-platinum transition hover:border-ducati-400\'>#{t} · {n}</a>';
fs.writeFileSync("src/pages/index.astro", L.join("\n"));
