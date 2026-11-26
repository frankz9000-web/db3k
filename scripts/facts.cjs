const fs = require("fs");
const ix = fs.readFileSync("src/pages/index.astro", "utf8").split("\n");
console.log("INDEX total lines:", ix.length);
ix.forEach((l, n) => { if (l.includes("TAG_COUNTS")) console.log(n + 1, "::", l.slice(0, 90)); });
const sl = fs.readFileSync("src/pages/posts/[...slug].astro", "utf8").split("\n");
console.log("SLUG total lines:", sl.length);
sl.forEach((l, n) => { if (/<Layout/.test(l)) console.log(n + 1, "::", l.slice(0, 120)); });
console.log("slug has ogImage prop string:", fs.readFileSync("src/pages/posts/[...slug].astro", "utf8").includes("ogImage="));
