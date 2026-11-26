/* Verifica visiva DB3K — screenshot full-page con Chrome di sistema */
const fs = require('fs');
const puppeteer = require('puppeteer-core');
const path = require('path');

const BASE = process.argv[2] || 'http://localhost:4321';
const CHROME = 'C:/Program Files/Google/Chrome/Application/chrome.exe';

const TARGETS = [
  { url: `${BASE}/`, file: 'shots/home-desktop.png', width: 1440 },
  { url: `${BASE}/posts/benvenuto`, file: 'shots/post-benvenuto.png', width: 1440 },
  { url: `${BASE}/`, file: 'shots/home-mobile.png', width: 390 },
];

(async () => {
  const browser = await puppeteer.launch({ executablePath: CHROME, headless: true });
  for (const t of TARGETS) {
    const page = await browser.newPage();
    await page.setViewport({ width: t.width, height: 900, deviceScaleFactor: 2 });
    await page.goto(t.url, { waitUntil: 'networkidle2' });
    await new Promise((r) => setTimeout(r, 600)); // fontanti + immagini locali
    const abs = path.join(__dirname, '..', t.file);
    fs.mkdirSync(path.dirname(abs), { recursive: true });
    await page.screenshot({ path: abs, fullPage: true });
    console.log('OK', t.file);
    await page.close();
  }
  await browser.close();
})().catch((e) => {
  console.error(e.message);
  process.exit(1);
});
