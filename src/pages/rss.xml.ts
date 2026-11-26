import { getCollection } from 'astro:content';

const site = 'https://db3k-blog.pages.dev';

export const GET = async () => {
  const collection = await getCollection('posts');
  const posts = Array.isArray(collection) ? collection : [...collection];

  const items = posts
    .sort((a, b) => new Date(b.data.pubDate).valueOf() - new Date(a.data.pubDate).valueOf());

  let xml = `<?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">
  <channel>
    <title>DB3K — Diario di Bordo</title>
    <link>${site}</link>
    <description>Gare, allenamenti e uscite DB3K in Sicilia.</description>`;

  for (const post of items) {
    const link = `${site}/posts/${post.id}/`;
    xml += `\n    <item>\n      <title><![CDATA[${post.data.title}]]></title>\n      <link>${link}</link>\n      <guid>${link}</guid>\n      <pubDate>${new Date(post.data.pubDate).toUTCString()}</pubDate>`;
    if (post.data.description) xml += `\n      <description><![CDATA[${post.data.description}]]></description>`;
    xml += `\n    </item>`;
  }

  xml += `\n  </channel>\n</rss>`;

  return new Response(xml, {
    headers: { 'Content-Type': 'application/xml' },
  });
};
