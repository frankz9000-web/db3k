const puppeteer = require('puppeteer-core');
(async () => {
  const b = await puppeteer.launch({ headless: 'new', executablePath: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe' });
  const p = await b.newPage();
  await p.setViewport({ width: 1440, height: 900 });
  await p.goto('https://db3k.netlify.app/', { waitUntil: 'networkidle2', timeout: 60000 });
  await new Promise((r) => setTimeout(r, 3500)); // tempo al POST verso CountAPI di rispondere (o fallire)
  await p.evaluate(() => window.scrollTo(0, document.body.scrollHeight));
  await new Promise((r) => setTimeout(r, 800));
  const meter = await p.$eval('#visitors-count', (el) => el.textContent).catch(() => 'HIDDEN');
  console.log('meter state:', meter);
  await p.screenshot({ path: 'C:\\Users\\frank\\Desktop\\blog db3k\\shots\\live-footer.png' });
  console.log('OK footer screenshot');
  await b.close();
})().catch((e) => {
  console.error('ERR', e.message);
  process.exit(1);
});
