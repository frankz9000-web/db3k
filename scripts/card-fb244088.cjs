const puppeteer = require('puppeteer-core');
(async () => {
  const b = await puppeteer.launch({ headless: 'new', executablePath: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe' });
  const p = await b.newPage();
  await p.setViewport({ width: 1440, height: 900 });
  await p.goto('http://localhost:4321/page/5/', { waitUntil: 'networkidle2', timeout: 30000 });
  await new Promise(r => setTimeout(r, 1200));
  const card = await p.evaluateHandle(() =>
    [...document.querySelectorAll('article')].find(a => a.querySelector('a[href*="buona-sera-amici-delle-ruote-fb244088"]'))
  );
  if (!card) { console.error('CARD NON TROVATA'); process.exit(1); }
  const el = card.asElement();
  await el.scrollIntoView();
  await new Promise(r => setTimeout(r, 1500));
  await el.screenshot({ path: 'C:\\Users\\frank\\Desktop\\blog db3k\\shots\\card-fb244088-anteprima.png' });
  const hasImgs = await p.evaluate(() => {
    const a = [...document.querySelectorAll('article')].find(a => a.querySelector('a[href*="buona-sera-amici-delle-ruote-fb244088"]'));
    return a ? a.querySelectorAll('img').length : -1;
  });
  console.log('OK — immagini nella card:', hasImgs);
  await b.close();
})().catch(e => { console.error('ERR ' + e.message); process.exit(1); });
