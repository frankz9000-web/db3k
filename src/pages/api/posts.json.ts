import type { APIRoute } from 'astro';
import { getCollection } from 'astro:content';

export const GET: APIRoute = async () => {
  const posts = (await getCollection('posts')).sort(
    (a, b) => b.data.pubDate.getTime() - a.data.pubDate.getTime()
  );

  const postsData = posts.map(post => ({
    slug: post.id,
    title: post.data.title,
    excerpt: post.data.description ?? null,
    content: post.body,
    pubDate: post.data.pubDate.toISOString(),
    updatedDate: post.data.updatedDate?.toISOString() ?? null,
    tags: post.data.tags ?? [],
    coverImage: post.data.image ?? null,
    coverAlt: null,
    author: 'DB3K Team',
    url: `https://db3k-blog.pages.dev/posts/${post.id}/`,
  }));

  return new Response(JSON.stringify({
    posts: postsData,
    total: postsData.length,
    page: 1,
    perPage: postsData.length,
  }), {
    status: 200,
    headers: {
      'Content-Type': 'application/json',
      'Cache-Control': 'public, max-age=3600',
      'Access-Control-Allow-Origin': '*',
    },
  });
};