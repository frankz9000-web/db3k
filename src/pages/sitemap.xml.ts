import { getCollection } from 'astro:content';

const site = 'https://db3k-blog.pages.dev';

export const GET = async () => {
  const collection = await getCollection('posts');
  const posts = Array.isArray(collection) ? collection : [...collection];

  const allTags = [
    ...new Set(posts.map((entry) => entry.data.tags ?? []).flat()),
  ].sort();

  const pages = ['/', '/albums/', '/reels/', '/tags/', ...allTags.map((tag) => `/tags/${encodeURIComponent(tag)}/`)];

  const postsSorted = posts
    .sort((a, b) => new Date(b.data.pubDate).valueOf() - new Date(a.data.pubDate).valueOf());

  let xml = `<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.w3.org/2005/Sitemap">`;

  for (const page of pages) {
    xml += `\n  <url><loc>${site}${page}</loc></url>`;
  }


  for (const post of postsSorted) {
    const loc = `${site}/posts/${post.id}/`;
    const lastmod = (post.data.updatedDate ?? post.data.pubDate);
    xml += `\n  <url><loc>${loc}</loc><lastmod>${new Date(lastmod).toISOString().split('T')[0]}</lastmod></url>`;
  }

  xml += `\n</urlset>`;

  return new Response(xml, {
    headers: { 'Content-Type': 'application/xml' },
  });
};
