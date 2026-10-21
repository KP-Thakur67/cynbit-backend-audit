# Security & Performance Findings

Severity scale: **Informational / Low / Medium / High**. No High or Critical issue was identified. Nothing here is a demonstrated or exploited vulnerability.

| ID | Title | Category | Severity | Status |
|---|---|---|---|---|
| F-01 | Server Action returns an opaque `{"success": bool}` | Error handling / API design | Low | Partially verified |
| F-02 | Server-side validation: partial indication only | Data validation | Informational | Partially verified |
| F-03 | Common hardening headers not seen on the captured response | Security hardening | Low | Partially verified |
| F-04 | Abuse controls (rate limit / bot protection) unverified | Security / availability | Low–Medium (potential) | Hypothesis |
| F-05 | Edge in Mumbai, function executed in a US region | Performance | Low | Partially verified |
| F-06 | Success response re-sends a full page render tree | Performance | Informational | Partially verified |
| F-07 | Third-party analytics and fonts; form-interaction tracking | Privacy / performance | Informational | Confirmed (observation) |

---
## F-01 Opaque Server Action result
- **Evidence:** E-005, E-006 (`{"success":true}`), E-007 (`{"success":false}`).
- **Observation:** Both outcomes return a small body, and the successes were HTTP 200 (the failure's status was not captured). The failure response carries no error code, field errors or reference ID.
- **Impact:** A client cannot tell a validation error from a server fault, so users get poor feedback and the team gets a weak monitoring signal. This is a design weakness, not a vulnerability.
- **Recommendation:** Return a typed result: `{ success:false, code:"VALIDATION_ERROR", fieldErrors:{...} }`, plus a correlation ID for unexpected faults. In a Spring API use `422` + RFC 9457 `ProblemDetail` (see `ApiExceptionHandler.java`).

## F-02 Server-side validation: partial indication
- **Evidence:** E-007 vs E-005/E-006; E-008.
- **Observation:** An email of `absc@gma` (no TLD) returned `success:false`; a normal email returned `success:true`. Browsers' built-in `type=email` check generally accepts `user@host`, so the request probably reached the server (P-8 would confirm).
- **What it suggests:** The server *may* apply stricter validation than the browser. This is an inference: other fields may have differed and the failure could have another cause.
- **Not claimed:** That validation is complete, or that it is absent.
- **Recommendation:** Validate on the server regardless (see `ContactRequest.java`). Hibernate's `@Email` also accepts `user@host`, so add a `@Pattern` requiring a dotted TLD if that is the policy.

## F-03 Hardening headers not seen on the captured response
- **Evidence:** E-005.
- **Present:** `Strict-Transport-Security: max-age=63072000` (two years; `includeSubDomains`/`preload` not seen). **Not seen on that response:** CSP, X-Content-Type-Options, X-Frame-Options, Referrer-Policy, Permissions-Policy.
- **Caveat:** The capture is a Server Action response (`text/x-component`), where several of these matter little. They matter most on HTML documents, which were not captured (P-1). DevTools' "Referrer policy" line is the request's policy, not a response header.
- **Impact:** Defence-in-depth only (clickjacking, MIME sniffing, XSS blast radius). No exploit shown.
- **Recommendation:** Add the headers in `next.config.js` (see `nextjs-equivalents.md`); roll CSP out as `Report-Only` first.

## F-04 Abuse controls unverified
- **Evidence:** None either way (P-2). A few submissions succeeded without a 429, which proves nothing about limits.
- **Hypothesis:** A public form that triggers email/CRM work can be flooded or spammed without a rate limit, bot check or honeypot. Not tested, because load testing is out of scope.
- **Recommendation:** Per-IP rate limit (`RateLimitFilter.java`), honeypot field, optional Turnstile/reCAPTCHA token verified server-side, and queue-based delivery so bursts do not hit the mail provider directly.

## F-05 Edge in Mumbai, function in a US region
- **Evidence:** E-005 and E-006: `x-vercel-id: bom1::iad1::…`.
- **Observation (my reading of Vercel's `edge::function::id` format — verify in Vercel's docs before submitting):** the request entered at the Mumbai edge, but the function appears to run in `iad1` (US East).
- **Hypothesis:** For India-based visitors this adds an intercontinental round trip to each Server Action. Latency was not measured (P-3).
- **Recommendation:** Set the function region to `bom1` (or next to the real dependency, such as the database or mail service) in `vercel.json`.

## F-06 Success response re-sends a render tree
- **Evidence:** E-005/E-006 bodies contain the full route tree before `{"success":true}`; the failure body (E-007) does not. `x-action-revalidated: [[],1,0]` is present.
- **Hypothesis:** A revalidation after success re-renders the page. If the form only needs a confirmation message, this is avoidable payload. Sizes not captured.
- **Recommendation:** Avoid unneeded `revalidatePath` or cookie mutations in the action.

## F-07 Third-party analytics and fonts
- **Evidence:** E-004, E-009.
- **Observation:** GA4 receives a `form_start` event including the first field's *name* and the field count (no values were seen). A Google Fonts stylesheet loads from `fonts.googleapis.com`. Only `_ga*` cookies were seen on requests to cynbit.in.
- **Impact:** Privacy and consent considerations (whether a consent notice exists was not recorded) and an extra third-party dependency for first paint.
- **Recommendation:** Confirm consent handling; self-host fonts with `next/font` if privacy/performance matters.

---
## Observed but deliberately **not** reported as findings
`server: Vercel`, `x-vercel-id`, `x-matched-path`, the `next-action` header, public JS chunk names and the JSON-LD business data are standard platform/framework metadata or intentional public SEO content. None is a vulnerability by itself.
