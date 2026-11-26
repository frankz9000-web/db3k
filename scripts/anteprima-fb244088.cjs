const puppeteer = require('puppeteer-core');
(async () => {
  const b = await puppeteer.launch({ headless: 'new', executablePath: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe' });
  const U = 'http://localhost:4321/posts/buona-sera-amici-delle-ruote-fb244088/';
  // desktop
  let p = await b.newPage();
  await p.setViewport({ width: 1440, height: 900 });
  await p.goto(U, { waitUntil: 'networkidle2', timeout: 30000 });
  await new Promise(r => setTimeout(r, 1500));
  await p.screenshot({ path: 'C:\\Users\\frank\\Desktop\\blog db3k\\shots\\anteprima-fb244088-desktop.png', fullPage: true });
  console.log('OK desktop full');
  await p.close();
  // mobile
  p = await b.newPage();
  await p.setViewport({ width: 390, height: 844 });
  await p.goto(U, { waitUntil: 'networkidle2', timeout: 30000 });
  await new Promise(r => setTimeout(r, 1500));
  await p.screenshot({ path: 'C:\\Users\\frank\\Desktop\\blog db3k\\shots\\anteprima-fb244088-mobile.png', fullPage: true });
  console.log('OK mobile full');
  await b.close();
})().catch(e => { console.error('ERR ' + e.message); process.exit(1); });
