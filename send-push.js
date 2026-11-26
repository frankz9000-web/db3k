#!/usr/bin/env node
/**
 * send-push.js — Test push notifications per DB3K Blog (iOS + Android).
 * 
 * Template predefiniti:
 *   1. Nuovo post sul blog
 *   2. Risultati gara DB3K
 *   3. Evento / uscita in programma
 *   4. Custom (titolo + messaggio personalizzati)
 *   5. Registra un token di test manuale (per test senza app)
 * 
 * Uso:
 *   node send-push.js              → menu interattivo
 *   node send-push.js "Titolo" "Messaggio" [URL]  → invio diretto
 */

import { createInterface } from 'readline';

const API_URL = 'https://db3k-blog.pages.dev/api/push/send';

// ─── Colori terminali ────────────────────────────────────────────────
const C = {
  reset: '\x1b[0m',
  green: '\x1b[32m',
  red:   '\x1b[31m',
  yellow:'\x1b[33m',
  cyan:  '\x1b[36m',
  dim:   '\x1b[2m',
};

function color(str, code) { return `${code}${str}${C.reset}`; }

// ─── Template predefiniti ────────────────────────────────────────────
const TEMPLATES = [
  {
    label: '📢 Nuovo post sul blog',
    title: 'Nuovo post DB3K!',
    body: 'È appena uscito un nuovo articolo sul blog. Apri l\'app per leggerlo.',
    url: '',
  },
  {
    label: '🏆 Risultati gara DB3K',
    title: 'Risultati gara aggiornati! 🏁',
    body: 'I risultati della prossima gara sono stati pubblicati. Controlla il blog per i dettagli.',
    url: '',
  },
  {
    label: '🎉 Evento / uscita in programma',
    title: 'Prossima uscita DB3K! 🚴‍♂️',
    body: 'È stata programmata una nuova uscita. Apri il blog per vedere dettagli e iscrizione.',
    url: '',
  },
];

// ─── Interfaccia da terminale ────────────────────────────────────────
function ask(question) {
  return new Promise((resolve) => {
    const rl = createInterface({ input: process.stdin, output: process.stdout });
    rl.question(color(question + ' ', C.cyan), (answer) => {
      rl.close();
      resolve(answer.trim());
    });
  });
}

async function menu() {
  console.log(`\n${color('📢 DB3K Push Notification Tester', C.green)}\n`);
  console.log(color('Scegli un template:', C.dim));
  
  for (let i = 0; i < TEMPLATES.length; i++) {
    const t = TEMPLATES[i];
    console.log(`  ${color(`${i + 1}.`, C.cyan)} ${t.label}`);
    console.log(color(`     "${t.title}" — "${t.body.substring(0, 60)}..."`, C.dim));
  }
  
  console.log(`  ${color('4.', C.cyan)} Custom (titolo + messaggio personalizzati)`);
  console.log(`  ${color('5.', C.cyan)} Registra token di test manuale`);
  console.log(`\n${color('Premi Invio per uscire', C.dim)}`);

  const choice = await ask('\n> ');
  
  if (choice === '4') {
    return customTemplate();
  } else if (choice === '5') {
    return registerTestToken();
  } else if (choice === '') {
    console.log(color('Uscito.', C.dim));
    process.exit(0);
  }

  const idx = parseInt(choice) - 1;
  if (idx >= 0 && idx < TEMPLATES.length) {
    return sendNotification(TEMPLATES[idx].title, TEMPLATES[idx].body, TEMPLATES[idx].url);
  } else {
    console.log(color('Scelta non valida. Riprova.', C.red));
    return menu();
  }
}

async function customTemplate() {
  const title = await ask('\nTitolo notifica: ');
  if (!title) { process.exit(0); }
  
  const body = await ask('Messaggio: ');
  if (!body) { process.exit(0); }
  
  const url = await ask('URL post (opzionale, Invio per saltare): ', '');
  
  return sendNotification(title, body, url || undefined);
}

async function registerTestToken() {
  console.log(`\n${color('Registra un token di test manuale', C.green)}`);
  console.log(color('(Usa questo quando vuoi testare senza l\'app installata)', C.dim));
  
  const token = await ask('\nIncolla il token Expo Push qui: ');
  if (!token) { process.exit(0); }
  
  const platform = await ask('Piattaforma (ios/android): ', 'ios');
  
  try {
    const res = await fetch(`${API_URL.replace('/send', '/register')}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ token, platform: platform.toLowerCase() }),
    });
    
    if (res.ok) {
      console.log(`\n${color('✅ Token registrato con successo!', C.green)}`);
      console.log(color(`   Token: ${token.substring(0, 20)}...`, C.dim));
      console.log(color(`   Ora puoi inviare notifiche a questo dispositivo.`, C.dim));
    } else {
      const err = await res.text();
      console.log(color(`❌ Errore: ${err}`, C.red));
    }
  } catch (e) {
    console.log(color(`❌ Errore di connessione: ${e.message}`, C.red));
  }
}

// ─── Invio notifica ──────────────────────────────────────────────────
async function sendNotification(title, body, url) {
  const payload = JSON.stringify({ title, body, url });
  
  console.log(`\n${color('📤 Invio notifica...', C.cyan)}`);
  console.log(color(`   Titolo: "${title}"`, C.dim));
  if (url) console.log(color(`   URL:    ${url}`, C.dim));
  console.log();

  try {
    const res = await fetch(API_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: payload,
    });
    
    if (!res.ok) {
      const errText = await res.text();
      console.log(color(`❌ HTTP ${res.status}: ${errText}`, C.red));
      process.exit(1);
    }
    
    const data = await res.json();
    
    if (data.error) {
      console.log(color(`   ⚠️  ${data.error}`, C.yellow));
    } else {
      console.log(color(`   ✅ Inviata a ${data.sent}/${data.total} dispositivo/i`, C.green));
      if (data.failed > 0) {
        console.log(color(`      (${data.failed} fallita/e)`, C.red));
      }
    }
  } catch (e) {
    console.log(color(`   ❌ Errore di rete: ${e.message}`, C.red));
    process.exit(1);
  }
  
  // Chiedi se inviare un altro
  const again = await ask('\n\nInvia un\'altra notifica? (s/N): ', '');
  if (again.toLowerCase() === 's') {
    return menu();
  }
}

// ─── Main ──────────────────────────────────────────────────────────────
(async () => {
  const [title, body, url] = process.argv.slice(2);

  if (title && body) {
    // Modalità diretta: node send-push.js "Titolo" "Messaggio" [URL]
    await sendNotification(title, body, url || undefined);
  } else {
    await menu();
  }
})();
