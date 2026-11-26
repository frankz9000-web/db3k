const fs = require("fs");
let L = fs.readFileSync("src/pages/index.astro", "utf8").split("\n");
let a = false, b = false;
for (let n = 0; n < L.length - 1 && !a; n++) { if (L[n].includes("[...m.entries()]") ) { for (let j = n + 1; j <= n + 4 && !a; j++) { if (L[j].trim() === "});" && j > 0) { L[j] = L[j].replace("});", "}());"); a = true; } } }
for (let n = 0; n < L.length && !b; n++) { if (L[n].includes("/tabs/")) { L[n] = L[n].replace("/tabs/", "/tags/"); b = true; } }
if (!a || !b) process.exit(44);
fs.writeFileSync("src/pages/index.astro", L.join("\n"));
console.log("ok");
