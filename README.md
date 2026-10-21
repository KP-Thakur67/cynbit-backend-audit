# Cynbit Backend Audit — Backend Developer Intern Assessment

**Author:** Kripal Singh  **Date of observation:** 21 Oct 2026  **Target:** https://www.cynbit.in (public marketing site)

## Scope and ethics
- Passive observation only, using a normal browser (Chrome DevTools) the way an ordinary visitor would.
- No injection, fuzzing, brute force, directory enumeration, load testing, auth bypass or access to other users' data.
- No endpoint was called that the site itself did not call. No request was replayed or modified.
- The public contact form was submitted a small number of times with obviously dummy data (see `evidence/EVIDENCE_LOG.md`). Nothing was repeated to test limits.

## Status labels used everywhere
| Label | Meaning |
|---|---|
| **Confirmed** | Directly supported by collected evidence |
| **Partially verified** | Some evidence exists, important details unknown |
| **Hypothesis / Recommendation** | Possible risk or best practice, not verified |

A missing header alone is never rated above Low.

## Repository layout
```
README.md                      this file
REPORT.md                      backend technical audit report (architecture, contract, limits)
INTERVIEW_NOTES.md             plain-language explanations for interview
findings/FINDINGS.md           categorized security + performance findings
evidence/EVIDENCE_LOG.md       sanitized evidence log + pending evidence
recommendations/
  nextjs-equivalents.md        fixes for the stack actually observed (Next.js on Vercel)
  spring-boot-reference/       Java 21 / Spring Boot reference implementation of the same fixes
```

## Running the Spring Boot reference
```bash
cd recommendations/spring-boot-reference
mvn clean verify
```
The reference code illustrates how the same controls look in a Java backend. It is **not** Cynbit's code; Cynbit's observed stack is Next.js.
