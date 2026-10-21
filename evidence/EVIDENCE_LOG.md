# Evidence Log (sanitized)

Redaction rules applied: no cookie values, no session or client IDs, no `x-client-data` / `x-browser-validation`, no server-action ID value, no personal data. Cookie **names** only. Public identifiers embedded in the page (GA measurement ID) are kept.

| ID | Page / endpoint | Method | Status | Sanitized observation | Can conclude | Cannot conclude | Verification |
|---|---|---|---|---|---|---|---|
| E-001 | SignalHire directory profile (third party) | n/a | n/a | Lists `cynbit.in` as website; Jaipur 302033; founded 2024 | Domain is associated with the company | Accuracy of the listing | Partially verified |
| E-002 | The Manifest directory profile (third party) | n/a | n/a | Cynbit Technologies Private Limited, Jaipur, launched 2024 | Independent confirmation of company and location | Anything about the tech stack | Partially verified |
| E-003 | Assessment email | n/a | n/a | Sender `no-reply@cynbit.in`; footer address Jaipur 302033 | Sender domain matches directories | Email authenticity (SPF/DKIM/DMARC not checked) | Partially verified |
| E-004 | `analytics.google.com/g/collect` (third party) | POST | 204 | Event `form_start` on `/contact`; metadata `form_length=6`, `first_field_name=email`, `first_field_position=2`; response has `Access-Control-Allow-Origin: https://www.cynbit.in`, `Cache-Control: no-cache, no-store, must-revalidate` | Site sends a GA4 event when a visitor starts the form; the form has 6 fields | That any form data reached Cynbit | Confirmed |
| E-005 | `https://www.cynbit.in/contact` (Server Action) | POST | 200 | Req: `multipart/form-data`, 763 bytes, `Accept: text/x-component`, `next-action` header present (value omitted), `Sec-Fetch-Site: same-origin`, cookies `_ga`, `_ga_LK6CQWCWX0` (names only). Resp: `content-type: text/x-component`, `content-encoding: br`, `cache-control: no-cache, no-store, max-age=0, must-revalidate`, `server: Vercel`, `strict-transport-security: max-age=63072000`, `x-vercel-cache: BYPASS`, `x-matched-path: /contact`, `x-action-revalidated: [[],1,0]`, `x-vercel-id: bom1::iad1::<redacted>`; body ends with `1:{"success":true}` | Contact form is a Next.js Server Action on Vercel; success path returns `{"success":true}` | Whether a message was stored or emailed | Confirmed |
| E-006 | same as E-005 (second submission, 09:49 GMT) | POST | 200 | Same headers; request body 699 bytes; body ends `1:{"success":true}`; `x-vercel-id: bom1::iad1::<redacted>` | Behaviour is consistent across submissions | Server-side validation | Confirmed |
| E-007 | same route, email entered as `absc@gma` (user-reported) | POST | not captured | Body: `0:{"a":"$@1","f":"","b":"<buildId>"}` then `1:{"success":false}`; no page tree, no error text | Server can return a failure result with no reason | Why it failed (validation, downstream error, other); other field values; headers | Partially verified |
| E-008 | `/contact` form in browser (user-reported, no screenshot) | n/a | n/a | Empty required field and an email without `@` are blocked by the browser with native messages | Client-side validation exists | That it is the only validation | Partially verified |
| E-009 | RSC payload inside E-005/E-006 response | n/a | n/a | `GoogleAnalytics` component with `gaId G-LK6CQWCWX0`; JSON-LD `ProfessionalService` (public business address/phone); stylesheet link to `fonts.googleapis.com` (Material Symbols); App Router route group `(marketing)`; chunks for `error` and `not-found` | Stack: Next.js App Router, GA4, Google Fonts | Next.js version; downstream services | Confirmed |

## Pending evidence (not collected — the report treats these as "not verified")
| ID | Item | Why it matters |
|---|---|---|
| P-1 | Response headers of `GET /` (document): CSP, X-Content-Type-Options, X-Frame-Options, Referrer-Policy, Permissions-Policy | Needed to judge F-03 properly |
| P-2 | Payload **field names** of POST /contact (no values); any CAPTCHA/honeypot/token field? | F-04 abuse controls |
| P-3 | "Waiting for server response" (TTFB) of the POST requests; Lighthouse scores | Quantify F-05 |
| P-4 | Status codes of `/robots.txt`, `/sitemap.xml`, `/.well-known/security.txt` | Public file hygiene |
| P-5 | Behaviour of one nonexistent URL (status + page) | Error handling |
| P-6 | Screenshot of browser validation messages | Backs E-008 |
| P-7 | Cookie flags from DevTools → Application (names/flags only) | Cookie hygiene |
| P-8 | Confirm the Payload of E-007 really contained `absc@gma` | Could upgrade F-02 |
