/**
 * POST /api/push/register — Registra un token FCM/Expo per ricezione notifiche.
 * 
 * Payload: { token, platform }  (platform = "android" | "ios")
 * Response: { ok: true } o { error: "..." }
 */

export async function onRequest(context) {
  const { request, env } = context;
  const kv = env?.PUSH_TOKENS;
  
  if (!kv) {
    return new Response(JSON.stringify({ error: 'KV namespace PUSH_TOKENS non configurato' }), {
      status: 500,
      headers: { 'Content-Type': 'application/json' },
    });
  }

  let payload;
  try {
    payload = await request.json();
  } catch (e) {
    return new Response(JSON.stringify({ error: 'Payload invalido' }), {
      status: 400,
      headers: { 'Content-Type': 'application/json' },
    });
  }

  const { token, platform } = payload;
  
  if (!token || !platform) {
    return new Response(JSON.stringify({ error: 'token e platform sono obbligatori' }), {
      status: 400,
      headers: { 'Content-Type': 'application/json' },
    });
  }

  // Salva il token nel KV con chiave = token stesso (per lookup diretto)
  await kv.put(token, JSON.stringify({ platform, registeredAt: new Date().toISOString() }), {
    expirationTtl: 30 * 24 * 60 * 60, // 30 giorni (FCM tokens scadono ~30gg)
  });

  return new Response(JSON.stringify({ ok: true }), {
    status: 200,
    headers: { 'Content-Type': 'application/json' },
  });
}
