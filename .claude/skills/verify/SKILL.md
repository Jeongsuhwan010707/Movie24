---
name: verify
description: Build, launch, and drive Movie24 end-to-end (signup/login/session/mypage) to confirm a change actually works, not just that it compiles.
---

# Movie24 verify recipe

Confirmed working 2026-07-26 against local MySQL (port 3306) + `application-local.properties`.

## Prerequisites

- Local MySQL running with a `movie24` schema reachable at `jdbc:mysql://localhost:3306/movie24`.
- `src/main/resources/application-local.properties` present (git-ignored) with `spring.datasource.username/password` and the three OAuth2 `client-id`/`client-secret` pairs (kakao/naver/google), or the equivalent `DB_USERNAME`/`DB_PASSWORD` env vars.
- Windows: use `gradlew.bat`.

## Launch

```bash
./gradlew.bat bootRun --console=plain
```

Wait for `Started Movie24Application` (~7s after Tomcat init). Flyway validates/migrates automatically — a `DbValidate` failure here means an entity is out of sync with the migrations (`ddl-auto=validate`), not an app bug.

Server listens on `http://localhost:8082`.

## Drive: session-auth flow (the load-bearing path)

**Gotcha — CSRF is required on every state-changing `/api/**` call.** This app does NOT use `CookieCsrfTokenRepository`; the token lives server-side in the session and is only exposed by rendering a page. `curl -X POST /api/...` with no token gets a bare `403 Forbidden` (not a validation error) — this is not a bug, it's CSRF doing its job. To drive the API from a shell, first GET a Thymeleaf page to mint a session + scrape the token:

```bash
curl -s -c cookies.txt http://localhost:8082/login -o login.html
CSRF=$(grep -oP 'name="_csrf"\s+value="\K[^"]+' login.html)
```

Then reuse `-b cookies.txt -c cookies.txt -H "X-CSRF-TOKEN: $CSRF"` on every subsequent request in the same curl session.

**Gotcha — run signup/login/logout curls in one Bash tool call.** Shell state (variables like `$CSRF`, `cookies.txt` in `/tmp`) does not persist between separate tool invocations — chain the whole flow in a single command block or the token/cookie will be silently empty on the next call.

**Gotcha — avoid non-ASCII (Korean) request bodies from Git Bash on Windows.** curl mangles multibyte UTF-8 in inline `-d '...'` strings under this shell (locale codepage), producing a false `HttpMessageNotReadableException: Invalid UTF-8 start byte`. Use ASCII-only test data, or pass `--data-binary @file.json` where `file.json` was written with Write/Edit (not shell heredoc).

Confirmed flow (real curl session, not MockMvc):

```bash
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8082/myPage         # anon -> 302 (redirect to /login)

curl -s -c c.txt http://localhost:8082/login -o l.html
CSRF=$(grep -oP 'name="_csrf"\s+value="\K[^"]+' l.html)

curl -s -b c.txt -c c.txt -H "X-CSRF-TOKEN: $CSRF" -H "Content-Type: application/json" \
  -d '{"loginId":"...","password":"...","name":"...","email":"..."}' \
  -X POST http://localhost:8082/api/users -w "\n%{http_code}\n"              # 201 on success

curl -s -b c.txt -c c.txt -H "X-CSRF-TOKEN: $CSRF" -H "Content-Type: application/json" \
  -d '{"loginId":"...","password":"wrong"}' \
  -X POST http://localhost:8082/api/login -w "\n%{http_code}\n"              # 401 on wrong password

curl -s -b c.txt -c c.txt -H "X-CSRF-TOKEN: $CSRF" -H "Content-Type: application/json" \
  -d '{"loginId":"...","password":"..."}' \
  -X POST http://localhost:8082/api/login -w "\n%{http_code}\n"              # 200 on success, session now authenticated

curl -s -b c.txt http://localhost:8082/myPage -o /dev/null -w "%{http_code}\n"   # auth -> 200

curl -s -b c.txt -c c.txt -H "X-CSRF-TOKEN: $CSRF" -X POST http://localhost:8082/api/logout -w "\n%{http_code}\n"  # 204
```

## Drive: social login (Kakao/Naver/Google)

Can't be curled — the provider redirect + consent screen requires a real browser. If a change touches `CustomOAuth2UserService`, `OAuth2UserInfoFactory`/`*OAuth2UserInfo`, `SecurityConfig`'s `oauth2Login(...)` block, or `PendingSocialSignupFilter`, manually verify in a browser:

1. `/login` → click each provider → confirm redirect + consent screen appears.
2. First-time social account → should land on `/users/new` with provider-supplied fields pre-filled/readonly, not directly on `/`.
3. Complete signup → should redirect to `/users/complete` and be logged in.
4. Log out, log back in with the same social account → should go straight to `/`, not `/users/new` again.
5. Naver specifically requires the browser to hit `127.0.0.1:8082`, not `localhost:8082` — Naver's redirect URI registration doesn't allow `localhost`.

## Tests (for reference, not a substitute for the above)

`./gradlew test` runs against in-memory H2 — no local MySQL needed. Useful as a fast regression check, but it doesn't touch the real MySQL/Flyway path or catch things like the CSRF behavior above (MockMvc's `TestSecurityContextHolder`/CSRF test support hides that).

`AuthFlowIntegrationTest#unauthenticatedRequestToProtectedPathIsRejected` verifies unauthenticated access to `/myPage` gets redirected to `/login` (`is3xxRedirection()` + `redirectedUrl(...)`), not a 4xx — the app has no formLogin, and `oauth2Login().loginPage("/login")` makes *every* unauthenticated protected request redirect regardless of `Accept` header (confirmed with curl against a live server, with and without `Accept: application/json`). Don't assert `is4xxClientError()` here — it doesn't happen.

**Environment gotcha (this sandbox only):** `./gradlew test` / `./gradlew.bat test` fails here with `Could not find or load main class worker.org.gradle.process.internal.worker.GradleWorkerMain`, reproducible even with `--no-daemon`, `--stop` first, from both Git Bash and PowerShell, on an unrelated/unmodified test suite. This is a Gradle test-worker-process spawn failure in this specific sandboxed shell, not a code issue — `compileTestJava` (no forked worker) succeeds fine, so use that to confirm test *sources* compile, and fall back to driving the app directly (per this skill) when the `test` task itself won't run here.
