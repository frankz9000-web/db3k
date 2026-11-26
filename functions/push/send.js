/**
 * POST /api/push/send — Invia notifiche push a tutti i dispositivi registrati.
 * 
 * Payload: { title, body, url }
 * Response: { sent, total, failed, error? }
 */

import { SignJWT, importPKCS8 } from 'jose';

export async function onRequest(context) {
  const { request, env } = context;
  const kv = env?.PUSH_TOKENS;
  
  if (!kv) {
    return new Response(JSON.stringify({ error: 'KV namespace PUSH_TOKENS non configurato' }), {
      status: 500,
      headers: { 'Content-Type': 'application/json' },
    });
  }

  // Leggo tutti i token dal KV namespace
  const keys = await kv.list();
  
  if (keys.keys.length === 0) {
    return new Response(JSON.stringify({ error: 'Nessun dispositivo registrato', sent: 0, total: 0 }), {
      status: 200,
      headers: { 'Content-Type': 'application/json' },
    });
  }

  // Leggo il secret FCM Service Account JSON
  const fcmKey = env.FCM_SERVICE_ACCOUNT_KEY_JSON;
  
  if (!fcmKey || fcmKey === '{}') {
    return new Response(JSON.stringify({ error: 'FCM_SERVICE_ACCOUNT_KEY_JSON non configurato', sent: 0, total: keys.keys.length }), {
      status: 500,
      headers: { 'Content-Type': 'application/json' },
    });
  }

  // Parse del payload
  let payload;
  try {
    payload = await request.json();
  } catch (e) {
    return new Response(JSON.stringify({ error: 'Payload invalido', sent: 0, total: 0 }), {
      status: 400,
      headers: { 'Content-Type': 'application/json' },
    });
  }

  const { title, body, url } = payload;
  
  if (!title || !body) {
    return new Response(JSON.stringify({ error: 'title e body sono obbligatori', sent: 0, total: 0 }), {
      status: 400,
      headers: { 'Content-Type': 'application/json' },
    });
  }

  // Autenticazione FCM con Service Account JSON
  const fcmToken = await getFcmAccessToken(fcmKey);
  
  if (!fcmToken) {
    return new Response(JSON.stringify({ error: 'Errore autenticazione FCM', sent: 0, total: keys.keys.length }), {
      status: 500,
      headers: { 'Content-Type': 'application/json' },
    });
  }

  // Invio a tutti i dispositivi
  let sent = 0;
  let failed = 0;
  
  for (const key of keys.keys) {
    const tokenData = await kv.get(key.name);
    if (!tokenData) continue;
    
    try {
      const data = JSON.parse(tokenData);
      
      // Android: FCM HTTP v1 API
      if (data.platform === 'android') {
        await sendFcmMessage(fcmToken, key.name, title, body, url);
        sent++;
      }
      // iOS: Expo Push API
      else if (data.platform === 'ios') {
        await sendExpoPush(data.token, title, body, url);
        sent++;
      }
    } catch (e) {
      failed++;
    }
  }

  return new Response(JSON.stringify({ 
    sent, 
    total: keys.keys.length, 
    failed 
  }), {
    status: 200,
    headers: { 'Content-Type': 'application/json' },
  });
}

/** Ottiene un access token FCM dal Service Account JSON */
async function getFcmAccessToken(fcmKeyJson) {
  try {
    const fcm = JSON.parse(fcmKeyJson);
    
    // Usa jose per firmare JWT con RS256
    const privateKey = await importPKCS8(pemToUint8Array(fcm.private_key), 'RS256');
    
    const token = await new SignJWT({ iss: fcm.client_email, scope: 'https://www.googleapis.com/auth/cloud-platform', aud: 'https://oauth2.googleapis.com/token', exp: Math.floor(Date.now() / 1000) + 3600 })
      .setProtectedHeader({ alg: 'RS256' })
      .sign(privateKey);
    
    const resp = await fetch('https://oauth2.googleapis.com/token', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: `grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=${token}`,
    });
    
    const result = await resp.json();
    return result.access_token;
  } catch (e) {
    console.error('FCM auth error:', e);
    return null;
  }
}

/** Invia messaggio FCM HTTP v1 */
async function sendFcmMessage(token, fcmToken, title, body, url) {
  const message = {
    message: {
      token: fcmToken,
      notification: {
        title,
        body,
      },
      data: url ? { url } : {},
    },
  };

  await fetch(`https://fcm.googleapis.com/v1/projects/db3k-blog/messages:send`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(message),
  });
}

/** Invia notifica Expo Push (iOS) */
async function sendExpoPush(expoToken, title, body, url) {
  await fetch('https://exp.host/--/api/v2/push/send', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify([{
      to: expoToken,
      title,
      body,
      data: url ? { url } : {},
    }]),
  });
}

/** Converte PEM private key a Uint8Array */
function pemToUint8Array(pem) {
  const b64 = pem.replace(/-----BEGIN (RSA )?PRIVATE KEY-----/, '').replace(/-----END (RSA )?PRIVATE KEY-----/, '').replace(/\s/g, '');
  return Uint8Array.from(atob(b64), c => c.charCodeAt(0));
}
