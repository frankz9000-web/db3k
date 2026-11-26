/**
 * Push notifications DB3K Blog — Cloudflare Pages Function (iOS + Android)
 * 
 * Endpoints:
 *   POST /api/push/register   → { token, platform }  → salva in KV
 *   POST /api/push/unregister → { token }            → rimuove da KV
 *   GET  /api/push/list       → elenca i token registrati (debug/test)
 *   POST /api/push/send       → { title, body, url?, platform? } → push a tutti i dispositivi registrati
 * 
 * Deploy: parte della build statica → npx wrangler pages deploy dist --project-name blog-db3k
 * KV binding: PUSH_TOKENS (wrangler.jsonc root) · Secret: FCM_SERVICE_ACCOUNT_KEY_JSON (Android)
 */

// ─── Helpers ──────────────────────────────────────────────────────────

function base64url(buf: ArrayBuffer): string {
  return btoa(String.fromCharCode(...new Uint8Array(buf)))
    .replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

async function signJWT(
  privateKeyPem: string,
  header: Record<string, unknown>,
  payload: Record<string, unknown>
): Promise<string> {
  // Import the PEM private key
  const pem = `-----BEGIN PRIVATE KEY-----\n${privateKeyPem}\n-----END PRIVATE KEY-----`;
  const keyData = atob(pem.replace(/-----[^-]+-----/g, '').replace(/\s/g, ''));
  
  // Use Web Crypto API to sign with RS256
  const encoder = new TextEncoder();
  const headerB64 = base64url(new TextEncoder().encode(JSON.stringify(header)));
  const payloadB64 = base64url(encoder.encode(JSON.stringify(payload)));
  const signingInput = `${headerB64}.${payloadB64}`;
  
  // Import private key (PEM format)
  const cryptoKey = await crypto.subtle.importKey(
    'pkcs8',
    pemToDer(pem),
    { name: 'RSASSA-PKCS1-v1_5', hash: 'SHA-256' },
    false,
    ['sign']
  );
  
  const signature = await crypto.subtle.sign(
    'RSASSA-PKCS1-v1_5',
    cryptoKey,
    encoder.encode(signingInput)
  );
  
  return `${headerB64}.${payloadB64}.${base64url(signature)}`;
}

function pemToDer(pem: string): Uint8Array {
  // Rimuovi header/footer PEM e tutti i whitespace
  const b64 = pem.replace(/-----BEGIN [^-]+-----/g, '')
                 .replace(/-----END [^-]+-----/g, '')
                 .replace(/[\s\r\n]/g, '');
  const binary = atob(b64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes;
}

// ─── Main handler ─────────────────────────────────────────────────────

export async function onRequest(context: { request: Request; env: Record<string, unknown> }) {
  const request = context.request;
  const env = context.env;

  // CORS preflight
  if (request.method === 'OPTIONS') {
    return new Response(null, {
      headers: {
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'POST, OPTIONS',
        'Access-Control-Allow-Headers': 'Content-Type',
      },
    });
  }

  const url = new URL(request.url);
  const path = url.pathname;

  if (path === '/api/push/register') return handleRegister(request, env);
  if (path === '/api/push/unregister') return handleUnregister(request, env);
  if (path === '/api/push/send') return handleSend(request, env);
  if (path === '/api/push/list') return handleList(env);

  return new Response('Not found', { status: 404 });
}

// ─── Register / Unregister ──────────────────────────────────────────

async function handleRegister(req: Request, env: Record<string, unknown>) {
  try {
    const body = await req.json();
    const token = body.token;
    if (!token || typeof token !== 'string') {
      return new Response(JSON.stringify({ error: 'Missing or invalid token' }), { status: 400 });
    }

    // Store in KV with expiry (tokens expire after ~30 days, re-register on next app open)
    await env.PUSH_TOKENS.put(token, JSON.stringify({
      platform: body.platform || 'ios',
      model: body.model || '',
      registeredAt: new Date().toISOString(),
    }), { expirationTtl: 2592000 }); // 30 days

    return new Response(JSON.stringify({ ok: true }));
  } catch (e) {
    console.error('handleRegister error:', e);
    return new Response(JSON.stringify({ error: 'Internal server error', details: String(e) }), { status: 500 });
  }
}

async function handleUnregister(req: Request, env: Record<string, unknown>) {
  const body = await req.json();
  if (body.token) {
    await env.PUSH_TOKENS.delete(body.token);
  }
  return new Response(JSON.stringify({ ok: true }));
}

// ─── List (debug/test: quanti token sono registrati) ─────────────────

async function handleList(env: Record<string, unknown>) {
  const keys = await env.PUSH_TOKENS.list();
  const devices = [];
  for (const key of keys.keys) {
    const metaRaw = await env.PUSH_TOKENS.get(key.name);
    if (!metaRaw) continue;
    const meta = typeof metaRaw === 'string' ? JSON.parse(metaRaw) : metaRaw;
    devices.push({
      token: key.name.slice(0, 24) + '…',
      platform: meta.platform,
      model: meta.model,
      registeredAt: meta.registeredAt,
    });
  }
  return new Response(JSON.stringify({ count: devices.length, devices }));
}

// ─── Send Push to all registered devices ────────────────────────────

async function handleSend(req: Request, env: Record<string, unknown>) {
  const body = await req.json();
  const { title, body: message, url, platform } = body;

  if (!title || !message) {
    return new Response(JSON.stringify({ error: 'Missing title or body' }), { status: 400 });
  }

  // Get all registered tokens from KV (iterate with cursor for large lists)
  const keys = await env.PUSH_TOKENS.list();
  if (!keys.keys.length) {
    return new Response(JSON.stringify({ sent: 0, total: 0, error: 'No registered devices' }));
  }

  let sent = 0;
  let failed = 0;

  for (const key of keys.keys) {
    const metaRaw = await env.PUSH_TOKENS.get(key.name);
    if (!metaRaw) continue;

    const meta = typeof metaRaw === 'string' ? JSON.parse(metaRaw) : metaRaw;
    if (platform && meta.platform !== platform) continue;

    try {
      if (meta.platform === 'android') {
        await sendFCM(env, key.name, title, message, url);
      } else {
        // iOS / Expo Push API
        await fetch('https://exp.host/--/api/v2/push/send', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            to: key.name,
            title,
            body: message,
            data: url ? { url } : {},
            sound: 'default',
          }),
        });
      }
      sent++;
    } catch (e) {
      failed++;
      console.warn(`Push failed for ${key.name} (${meta.platform}):`, e.message);
    }
  }

  return new Response(JSON.stringify({ 
    sent, 
    total: keys.keys.length, 
    failed,
    ...(failed > 0 ? { error: `${failed} push(es) failed` } : {})
  }));
}

// ─── FCM HTTP v1 API (Android) — usa Web Crypto nativo, zero dipendenze ──

async function sendFCM(env: Record<string, unknown>, token: string, title: string, message: string, url?: string) {
  const saKey = JSON.parse(env.FCM_SERVICE_ACCOUNT_KEY_JSON);
  
  // Get access token from service account key using Web Crypto API (no jose needed!)
  const accessToken = await getAccessToken(saKey);
  
  const projectId = saKey.project_id;
  const fcmUrl = `https://fcm.googleapis.com/v1/projects/${projectId}/messages:send`;

  const payload = {
    message: {
      token,
      notification: {
        title,
        body: message,
      },
      data: url ? { url } : {},
      android: {
        priority: 'high',
        notification: {
          channelId: 'db3k_blog',
        },
      },
    },
  };

  const res = await fetch(fcmUrl, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${accessToken}`,
    },
    body: JSON.stringify(payload),
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`FCM error ${res.status}: ${text}`);
  }
}

async function getAccessToken(saKey: Record<string, string>) {
  // Create a JWT signed with the service account's private key using Web Crypto API
  
  const now = Math.floor(Date.now() / 1000);
  
  const header = { alg: 'RS256', typ: 'JWT' };
  const payload = {
    iss: saKey.client_email,
    scope: 'https://www.googleapis.com/auth/cloud-platform',
    aud: 'https://oauth2.googleapis.com/token',
    exp: now + 3600, // 1 hour
    iat: now,
  };

  const jwt = await signJWT(saKey.private_key, header, payload);

  const res = await fetch('https://oauth2.googleapis.com/token', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: `grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=${jwt}`,
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`OAuth2 token exchange failed (${res.status}): ${text}`);
  }

  const data = await res.json();
  return data.access_token;
}
