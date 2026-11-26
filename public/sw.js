const CACHE_NAME = 'db3k-blog-v1';
const URLS_TO_CACHE = [
  '/',
  '/pillole/',
  '/albums/',
  '/reels/',
  '/tags/',
  '/chi-siamo/',
  '/contatti/',
];

// Install: cache the shell
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(URLS_TO_CACHE))
  );
});

// Activate: clean old caches
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(keys.filter((k) => k !== CACHE_NAME).map((k) => caches.delete(k)))
    )
  );
});

// Fetch: cache-first for HTML, network-first for API
self.addEventListener('fetch', (event) => {
  const url = new URL(event.request.url);

  // API calls → network first
  if (url.pathname.startsWith('/api/')) {
    event.respondWith(
      fetch(event.request).catch(() => caches.match(event.request))
    );
    return;
  }

  // HTML pages → cache first, fallback to network
  if (event.request.headers.get('accept')?.includes('text/html')) {
    event.respondWith(
      caches.match(event.request).then((cached) => {
        const fetchPromise = fetch(event.request).then((response) => {
          // Update cache on successful response
          if (response.ok) {
            const clone = response.clone();
            caches.open(CACHE_NAME).then((cache) => cache.put(event.request, clone));
          }
          return response;
        }).catch(() => null);

        return cached || fetchPromise;
      })
    );
    return;
  }

  // Everything else → network first
  event.respondWith(fetch(event.request).catch(() => caches.match(event.request)));
});
