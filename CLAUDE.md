# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Movie24 (`project.movie24`) — a Spring Boot 3.2 / Java 17 movie-theater booking site (server-rendered Thymeleaf views + a JSON API), backed by MySQL with Flyway migrations.

## Commands

```bash
./gradlew bootRun              # run the app (http://localhost:8082)
./gradlew test                 # run all tests (uses in-memory H2, no local MySQL needed)
./gradlew test --tests "project.movie24.user.service.UserServiceTest"       # single test class
./gradlew test --tests "*.UserServiceTest.someMethod"                        # single test method
./gradlew build                # compile + test + package
```

On Windows use `gradlew.bat` instead of `./gradlew`.

### Local setup

- Copy `application-local.properties` template values into your own copy (it's git-ignored) or set `DB_USERNAME`/`DB_PASSWORD` env vars, plus OAuth2 `client-id`/`client-secret` for kakao/naver/google.
- A local MySQL `movie24` schema is required for `bootRun` (Flyway migrates it on startup); tests don't need MySQL — they run against H2 (`src/test/resources/application.properties`).
- `TestDataInit` seeds a `test`/`test!` local-login user on every `ApplicationReadyEvent` (dev convenience, guarded by an existence check).

## Architecture

### Vertical feature packages

Code is organized by domain feature, not by layer: `movie`, `theater`, `screen`, `showtime`, `seat`, `reservation`, `user`, `mypage`, `help`, `store`, each typically with its own `controller/domain/dto/repository/service`. Top-level `project.movie24` holds app wiring (`Movie24Application`, `WebConfig`, `TestDataInit`), and `web/` holds cross-cutting MVC concerns (`ApiExceptionHandler`, `LogInterceptor`).

### View controllers vs. API controllers

Most features expose **two parallel controllers**: a `Controller` returning Thymeleaf view names (e.g. `LoginController`, `UserController`) and an `...ApiController` returning JSON under `/api/**` (e.g. `AuthApiController`, `MovieApiController`, `ReservationApiController`). Both paths converge on the same `service`/`repository` layer. When adding a feature, check whether it needs both a page and an API endpoint, or just one.

`ApiExceptionHandler` is a `@RestControllerAdvice` scoped to specific API controller packages only (listed explicitly in its `basePackages`) — it does not apply to view controllers. New `...ApiController` packages must be added to that list to get consistent JSON error responses (400 on validation, 404 on `IllegalArgumentException`, 409 on `IllegalStateException`/duplicate key).

### Auth: session-based, not JWT

Spring Security uses standard HTTP-session auth (`HttpSessionSecurityContextRepository`), shared by both view login (`LoginController`) and REST login (`AuthApiController`) — both authenticate via `AuthenticationManager` and save into the same `SecurityContextRepository`. CSRF is left enabled (session-stored token); Thymeleaf forms carry a hidden `_csrf` field. If `/api` endpoints start being called from JS via `fetch` instead of form posts, the token needs to be exposed (e.g. meta tag) and sent as a header.

Route authorization is centralized in `SecurityConfig.filterChain` — new public routes must be added to the `permitAll()` matchers there; everything else defaults to `authenticated()`.

### Social login (Kakao / Naver / Google)

All three providers funnel through one `CustomOAuth2UserService`, which uses `OAuth2UserInfoFactory`/`OAuth2UserInfo` implementations per provider to normalize attributes into the app's `User` shape. Key details:

- Google's OAuth2 scope deliberately excludes `openid` (see `application.properties`) — including it would make Spring Security treat it as OIDC and route through the built-in `OidcUserService` instead of `CustomOAuth2UserService`, bypassing DB integration.
- A **first-time** social login is not persisted immediately: `CustomOAuth2UserService` returns a `CustomOAuth2User` wrapping a transient `User` with `id == null`. `SecurityConfig`'s oauth2 success handler and `PendingSocialSignupFilter` both check for `id == null` and redirect to `/users/new` until the user completes signup (`UserController#create`), which persists the real `User` and re-authenticates with the saved principal. Any new route that should be reachable mid-signup needs to be added to the allowlist in `PendingSocialSignupFilter`.
- On signup completion, read-only fields shown for social users are populated from the provider-supplied `pendingSocialUser`, not from client-submitted form values (avoids trusting client input for identity fields).

### Database

Schema is managed by Flyway (`src/main/resources/db/migration/V*__*.sql`), with `ddl-auto=validate` — entities must match migrations exactly; schema changes go through a new `V{n}__description.sql` file, never through Hibernate auto-DDL. `baseline-on-migrate=true` / `baseline-version=1` because V1 represents a pre-existing schema that was originally built with `ddl-auto=update`.

### Tests

Tests run against H2 in `MySQL` compatibility mode (no local MySQL dependency). `AuthFlowIntegrationTest` is the reference pattern for auth-related integration tests: full `@SpringBootTest` + `MockMvc`, asserting on real session/cookie behavior rather than mocking security.
