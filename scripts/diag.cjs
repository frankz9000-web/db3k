const fs = require("fs");
let s = fs.readFileSync("src/pages/index.astro", "utf8");
console.log("len:", s.length);
const i = s.indexOf('return [...m.entries()]');
console.log("i:", i, "hasTabs:", s.includes("/tabs/"), "t:", s.indexOf("/tabs/"));
if (i >= 0) {
  const k = i + s.slice(i).indexOf("});");
  console.log("k:", k);
}
