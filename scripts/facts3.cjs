const fs = require("fs");
const p = "dist/.prerender/chunks/";
let f;
for (const c of fs.readdirSync(p)) { if (c.startsWith("index_") && !c.includes("tags")) { const t = fs.readFileSync(p + c, "utf8"); if (t.includes("TAG_COUNTS")) { f = c; break; } } }
if (!f) process.exit(42);
const L = fs.readFileSync(p + f, "utf8").split("\n");
console.log("--- chunk 30-61 ---");
for (let n = 29; n < L.length && n <= 60; n++) console.log(n + 1, "::", JSON.stringify(L[n].slice(0, 400)));
