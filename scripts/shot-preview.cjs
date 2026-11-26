const puppeteer = require('puppeteer-core');
(async () => {
  const b = await puppeteer.launch({ headless: 'new', executablePath: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe' });
  const p = await b.newPage();
  await p.setViewport({ width: 1440, height: 900 });
  const R = 'file:///C:/Users/frank/Desktop/blog%20db3k/dist/';
  const t = 1500;
  const go = async (u, f) => { await p.goto(R + u, { waitUntil: 'networkidle2', timeout: 30000 }); await new Promise(r => setTimeout(r, t)); await p.screenshot({ path: f }); console.log('OK -> ' + f); };
  await go('index.html', 'C:\\Users\\frank\\Desktop\\blog db3k\\shots\\preview-home.png');
  await go('tags/caltanissetta/index.html', 'C:\\Users\\frank\\Desktop\\blog db3k\\shots\\preview-tags-caltanissetta.png');
  await b.close();
})().catch(e => { console.error('ERR ' + e.message); process.exit(1); });
