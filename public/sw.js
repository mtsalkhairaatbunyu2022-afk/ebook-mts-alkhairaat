const CACHE_NAME = 'ebook-mts-v2.9';
const CDN_CACHE_NAME = 'ebook-mts-cdn-v2';

const urlsToCache = [
  '/',
  '/index.html',
  '/admin.html',
  '/admin',
  '/logo_alkhairaat.jpg',
  '/catalog.json',
  '/video.json',
  '/students.json',
  '/teachers.json',
  '/manifest.json',
  '/manifest-admin.json'
];

self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => Promise.allSettled(urlsToCache.map(url => cache.add(url))))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', event => {
  const validCaches = new Set([CACHE_NAME, CDN_CACHE_NAME]);
  event.waitUntil(
    caches.keys()
      .then(cacheNames => Promise.all(
        cacheNames.map(cacheName => {
          if (!validCaches.has(cacheName)) {
            return caches.delete(cacheName);
          }
        })
      ))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('message', event => {
  if (event.data === 'skipWaiting') {
    self.skipWaiting();
  }
});

self.addEventListener('fetch', event => {
  if (event.request.method !== 'GET') return;

  const url = new URL(event.request.url);

  // Never intercept Google Drive, Docs, YouTube, or usercontent requests
  if (
    url.hostname.includes('google.com') ||
    url.hostname.includes('googleusercontent.com') ||
    url.hostname.includes('gstatic.com') ||
    url.hostname.includes('youtube.com') ||
    url.hostname.includes('youtube-nocookie.com') ||
    url.hostname.includes('youtu.be')
  ) {
    return;
  }

  // Cache-First for CDN libraries (Bootstrap, PDF.js, CMaps, XLSX) so offline reader works without internet
  if (
    url.hostname === 'cdn.jsdelivr.net' ||
    url.hostname === 'cdnjs.cloudflare.com'
  ) {
    event.respondWith(
      caches.match(event.request).then(cached => {
        if (cached) return cached;
        return fetch(event.request).then(response => {
          if (response && (response.ok || response.type === 'opaque')) {
            const copy = response.clone();
            caches.open(CDN_CACHE_NAME).then(cache => cache.put(event.request, copy));
          }
          return response;
        });
      })
    );
    return;
  }

  const isJsonRequest = url.pathname.endsWith('.json');

  // Network-First for HTML navigation and JSON catalogs so updates & existing books are always fresh when online, with offline fallback
  if (
    isJsonRequest ||
    event.request.mode === 'navigate' ||
    (event.request.headers.get('accept') && event.request.headers.get('accept').includes('text/html'))
  ) {
    event.respondWith(
      fetch(event.request)
        .then(response => {
          if (response && response.ok) {
            const copy = response.clone();
            caches.open(CACHE_NAME).then(cache => cache.put(event.request, copy));
          }
          return response;
        })
        .catch(async () => {
          const cached = await caches.match(event.request);
          if (cached) return cached;
          if (url.pathname === '/admin') {
            return caches.match('/admin.html');
          }
          return caches.match('/index.html');
        })
    );
    return;
  }

  event.respondWith(
    caches.match(event.request)
      .then(response => {
        if (response) return response;
        return fetch(event.request).then(networkRes => {
          if (networkRes && networkRes.ok && url.origin === self.location.origin) {
            const copy = networkRes.clone();
            caches.open(CACHE_NAME).then(cache => cache.put(event.request, copy));
          }
          return networkRes;
        });
      })
  );
});
