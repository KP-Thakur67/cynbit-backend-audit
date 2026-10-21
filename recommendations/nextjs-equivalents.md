# Fixes for the stack actually observed (Next.js on Vercel)

> Illustrative. Field names are examples; the real field names were not captured (P-2).

## 1. Security headers (F-03) — `next.config.js`
```js
const securityHeaders = [
  { key: 'X-Content-Type-Options', value: 'nosniff' },
  { key: 'X-Frame-Options', value: 'DENY' },
  { key: 'Referrer-Policy', value: 'strict-origin-when-cross-origin' },
  { key: 'Permissions-Policy', value: 'camera=(), microphone=(), geolocation=()' },
  { key: 'Strict-Transport-Security', value: 'max-age=63072000; includeSubDomains; preload' },
  // Start in report-only; tighten after reviewing violations.
  { key: 'Content-Security-Policy-Report-Only', value:
      "default-src 'self'; " +
      "script-src 'self' 'unsafe-inline' https://www.googletagmanager.com; " +
      "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
      "font-src 'self' https://fonts.gstatic.com; " +
      "img-src 'self' data: https://www.google-analytics.com; " +
      "connect-src 'self' https://*.google-analytics.com https://analytics.google.com; " +
      "frame-ancestors 'none'" },
];
module.exports = { async headers() { return [{ source: '/(.*)', headers: securityHeaders }]; } };
```
Add `includeSubDomains`/`preload` to HSTS only if every subdomain is HTTPS-only.

## 2. Validated Server Action with a structured result (F-01, F-02, F-04)
```ts
'use server';
import { z } from 'zod';

const schema = z.object({
  name: z.string().trim().min(1).max(100),
  email: z.string().trim().max(254).regex(/^[^@\s]+@[^@\s]+\.[A-Za-z]{2,}$/),
  message: z.string().trim().min(10).max(2000),
  website: z.string().optional(),          // honeypot, must stay empty
});

export type ContactState =
  | { success: true; reference: string }
  | { success: false; code: 'VALIDATION_ERROR'; fieldErrors: Record<string, string[] | undefined> }
  | { success: false; code: 'RATE_LIMITED' | 'INTERNAL_ERROR'; reference?: string };

export async function submitContact(_prev: ContactState | null, formData: FormData): Promise<ContactState> {
  const parsed = schema.safeParse(Object.fromEntries(formData));
  if (!parsed.success) {
    return { success: false, code: 'VALIDATION_ERROR', fieldErrors: parsed.error.flatten().fieldErrors };
  }
  if (parsed.data.website) {                // bot: pretend success, do nothing
    return { success: true, reference: crypto.randomUUID() };
  }
  // TODO: rate-limit by IP (e.g. Upstash Ratelimit), then enqueue the email/CRM job.
  return { success: true, reference: crypto.randomUUID() };
}
```

## 3. Function region (F-05) — `vercel.json`
```json
{ "regions": ["bom1"] }
```
Choose the region closest to the database/mail provider the action really depends on. Check the plan's region limits.

## 4. Avoid needless re-render (F-06)
Do not call `revalidatePath` / mutate cookies in the action unless the page truly changes. Show the confirmation from the returned state.
