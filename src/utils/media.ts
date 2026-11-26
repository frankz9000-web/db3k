import { existsSync, readdirSync } from 'node:fs';
import path from 'node:path';

export interface PostMedia {
  video?: string;
  poster?: string;
  photos: string[];
  /** SVG animato (anteprima animata del post). */
  animatedSvg?: string;
}

/**
 * Elenca i media reali di un post dentro public/fb/<post-id>/
 * (video.mp4 + poster, oppure foto-01..N.jpg) e li restituisce
 * come anteprima animata per la card.
 */
export function getPostMedia(postId: string, publicDir: string, mediaDir?: string): PostMedia {
  const empty: PostMedia = { photos: [] };
  const slug = mediaDir ?? postId;
  const dir = path.join(publicDir, 'fb', slug);
  if (!existsSync(dir)) return empty;

  const files = readdirSync(dir).sort();
  const url = (f: string) => `/fb/${slug}/${f}`;

  // SVG animato ha priorità su video/foto (anteprima animata del post)
  const svg = files.find((f) => /\.svg$/i.test(f));
  if (svg) return { animatedSvg: url(svg), photos: [] };

  const video = files.find((f) => /\.(mp4|webm|mov)$/i.test(f));
  const poster = files.find((f) => f === 'poster.jpg') ?? files.find((f) => f === 'video.jpg');

  const photos = files
    .filter((f) => /^foto-\d+\.jpe?g$/i.test(f))
    .slice(0, 3)
    .map(url);

  if (!video && photos.length === 0) {
    const other = files.filter((f) => /\.(jpg|jpeg|png|webp)$/i.test(f)).slice(0, 3).map(url);
    return { photos: other };
  }

  return {
    video: video ? url(video) : undefined,
    poster: poster ? url(poster) : undefined,
    photos,
  };
}
