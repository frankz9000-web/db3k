// Contatore visite DB3K — Pages Function + KV (autonomo, niente terzi).
// GET /api/hit → incrementa e restituisce { value: <n> }.
// Chiave unica "diario-di-bordo" = contatore globale del sito (come l'Abacus).

const KEY = 'diario-di-bordo';
const TTL = 60 * 60 * 24 * 365 * 10; // 10 anni

export async function onRequest(context) {
  const { env, next } = context;
  const VISITS = env?.VISITS;
  if (!VISITS) return next();

  let count = 0;
  try {
    const raw = await VISITS.get(KEY);
    if (raw !== null) count = Math.max(0, parseInt(raw, 10) || 0);
  } catch {
    /* se la lettura fallisce, ripartiamo da 0 ma incrementiamo lo stesso */
  }
  count += 1;

  try {
    await VISITS.put(KEY, String(count), { expirationTtl: TTL });
  } catch {
    /* write-through best effort: rispondiamo comunque */
  }

  return new Response(JSON.stringify({ value: count }), {
    headers: {
      'content-type': 'application/json',
      'cache-control': 'no-store',
      // stesso-origine dal blog; questi header aiutano se un giorno lo si chiama da un'altra origine
      'access-control-allow-origin': '*',
    },
  });
}
