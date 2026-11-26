import { readFileSync } from 'fs';

const html = readFileSync('./blog-output.html', 'utf8');

console.log('=== HTML SIZE ===');
console.log(html.length, 'bytes');

console.log('\n=== POST LINKS ===');
const postLinks = html.match(/<a[^>]*href="(\/posts\/[^"]+)"[^>]*>/g) || [];
console.log(postLinks.length, 'post links found');
postLinks.forEach(l => {
  const href = l.match(/href="([^"]+)"/)?.[1];
  console.log(' -', href);
});

console.log('\n=== VISITORS COUNT ===');
const countMatch = html.match(/id="visitors-count"[^>]*>([^<]+)</);
console.log(countMatch ? countMatch[1] : 'NOT FOUND');

console.log('\n=== COMPONENTS ===');
console.log('Has Hero:', html.includes('Hero'));
console.log('Has PostCard:', html.includes('PostCard'));
console.log('Has Footer:', html.includes('footer'));
console.log('Has Nav:', html.includes('Articoli') && html.includes('Album'));
console.log('Has Tailwind CSS:', html.includes('tailwind'));
console.log('Has hit script:', html.includes('api/hit'));

console.log('\n=== IMAGES ===');
const imgTags = html.match(/<img[^>]+>/g) || [];
console.log(imgTags.length, 'img tags');
imgTags.forEach(img => {
  const src = img.match(/src="([^"]+)"/)?.[1];
  const alt = img.match(/alt="([^"]*)"/)?.[1];
  if (src) console.log(' -', src, alt ? '(' + alt + ')' : '');
});

console.log('\n=== PAGE STRUCTURE ===');
console.log('Has <header>:', html.includes('<header'));
console.log('Has <main>:', html.includes('<main'));
console.log('Has <footer>:', html.includes('<footer'));
console.log('Has <nav>:', html.includes('<nav'));

console.log('\n=== HERO SECTION ===');
const heroMatch = html.match(/<astro-is[^>]*name="Hero"[^>]*\/>/);
console.log('Has Hero Astro component:', !!heroMatch);

console.log('\n=== CHECKING FOR BROKEN REFERENCES ===');
const brokenImg = html.match(/src="(\/[^"]*\.(jpg|png|webp|svg))"[^>]*>/g) || [];
console.log('Image src refs:', brokenImg.length);
brokenImg.forEach(ref => {
  const src = ref.match(/src="([^"]+)"/)?.[1];
  if (src) console.log('  img:', src);
});
