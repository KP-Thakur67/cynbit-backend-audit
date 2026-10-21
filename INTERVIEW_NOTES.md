# Interview notes (plain language)

**What did you actually find?** Mostly good practice plus hardening gaps. HTTPS and HSTS are in place. The form is a Next.js Server Action on Vercel. Its result is just `{"success": true/false}`, which hides *why* a request failed. I found no demonstrated vulnerability and I said so.

**What is a Server Action?** A function that runs on the server and that the browser calls through a framework-managed POST. Here that is `POST /contact` with the `next-action` header. It still needs the same validation, rate limiting and error design as a REST endpoint.

**Why not just call a missing header a vulnerability?** A missing header is a hardening gap, not an exploit. Severity depends on impact and exploitability. I rated these Low and marked them Partially verified because I had only captured one response type.

**How do you know server-side validation exists?** I don't. A no-TLD email returned `success:false`, which suggests the server checks the email, but other fields may have differed. I labelled that Partially verified. The browser check alone cannot be trusted because anyone can bypass the browser.

**Why Spring Boot snippets if the site is Next.js?** They show the same controls in my stack: Bean Validation, `ProblemDetail` for errors, a security headers config and a rate-limit filter. `nextjs-equivalents.md` has the fixes for the real stack.

**Why does `@Email` accept `user@host`?** Hibernate Validator follows the RFC loosely and allows hostnames without a dot. If the business rule is "needs a TLD", add a `@Pattern`.

**Why 422 and not 400?** 400 means the request could not be parsed; 422 means it parsed but the data failed validation. Either is defensible if used consistently.

**Why is in-memory rate limiting not enough?** It is per instance. With several instances or serverless functions, use a shared store (Redis) or a gateway/WAF.

**What would you do next with more access?** Check `GET /` headers, measure TTFB per region, read server logs for the failure reason, and test limits with the owner's written permission.
