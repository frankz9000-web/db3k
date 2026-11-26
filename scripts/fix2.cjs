const fs = require("fs");
let s = fs.readFileSync("src/pages/index.astro", "utf8");
// Fix 1: invoke the IIFE (bare arrow function -> call expression)
const i = s.indexOf('return [...m.entries()]');
if (i < 0) process.exit(42);
const k = i + s.slice(i).indexOf("});");
if (k < 0 || !/^[ \t]*$/.test(s.slice(k - 3, k).slice(-1))) { /* sanity: preceding line must be the return-line area */ }
if (k <= i) process.exit(42);
s = s.slice(0, k) + "})();";
// Fix 2: nav href must point at /tags/ (real route), not /tabs/
const t = s.indexOf("/tabs/");
if (t < 0) process.exit(43);
s = s.replace("}/tabs/", "/tags/").replace("\n{", "\n{") ; // no-op safety
s = s.slice(0, t) + "/tags/" + s.slice(t + 6);
fs.writeFileSync("src/pages/index.astro", s);
console.log("ok");
