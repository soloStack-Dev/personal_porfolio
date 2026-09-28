# AGENTS.md

## Layout: the project is one level down

Everything lives in `demo/`. That is the Maven project root. Run all Maven commands with
`workdir=demo` (or `cd demo` first). There is nothing else in this repo — no CI, no Dockerfile,
no linter/formatter config, no README. No git repo is initialized here yet.

## Stack & commands

Spring Boot **4.1.1**, Java **21**, Maven wrapper pinned to Maven 3.9.16 (`distributionType=only-script`).
Use the wrapper, not a system `mvn`:

```powershell
.\mvnw.cmd spring-boot:run      # dev server, http://localhost:8080 (devtools live-reloads on classpath change)
.\mvnw.cmd test                 # 27 tests: PortfolioPageTests (21) + ContactMailConfigurationTests (5) + DemoApplicationTests (1)
.\mvnw.cmd test -Dtest=PortfolioPageTests
.\mvnw.cmd package              # produces the runnable jar in target/
```

`mvnw.cmd` verifies fine (`.\mvnw.cmd -v`). Verification order when something is broken: `test` first
(it is cheap and catches context/bean wiring errors), then `package`. There is no lint or typecheck
step to run. Never edit anything under `demo/target/`.

**Spring Boot 4 starter names are not the Spring Boot 3 ones.** This pom uses
`spring-boot-starter-webmvc` (not `-web`) and a per-module test starter
(`spring-boot-starter-webmvc-test`, `-thymeleaf-test`, `-validation-test`, `-mail-test`,
`-actuator-test`). Do not "correct" these to legacy artifact IDs — the build will break.

## Docker

`Dockerfile` (multi-stage: `maven:3.9.16-eclipse-temurin-21` builds, `eclipse-temurin:21-jre-jammy`
runs) plus `docker-compose.yml`, both at the `demo/` root. `.dockerignore` keeps `target/` and
`.git/` out of the build context so the dependency layer stays cached.

```powershell
docker compose up -d --build   # build + run; service name is `portfolio`
docker compose ps              # STATUS shows (healthy) once the first check passes
docker compose logs -f         # contact-form submissions appear here when no SMTP is configured
docker compose down
```

- Host port comes from `PORT` (`${PORT:-8080}`), container always listens on 8080. The container
  id is `portfolio-demo`; image is `portfolio-demo:latest`.
- Runs as uid 65534 (`nobody`), `MaxRAMPercentage=75` against a 512M compose limit, `curl` present
  only for the healthcheck.
- **Healthcheck requests `/`, not `/actuator/health`.** Actuator aggregates every indicator, and
  `MailHealthIndicator` throws when no relay is configured — the default state — so the aggregate
  reports DOWN while the site is fine. Checking `/` verifies the thing that matters: Thymeleaf
  renders.
- **Never list `SPRING_MAIL_*` in compose `environment:` with empty or `true` defaults.** An empty
  `spring.mail.host` still counts as "present" to Spring Boot, which then builds a `JavaMailSender`
  and defeats the no-config fallback where `ContactMailService` logs the enquiry instead of
  sending. SMTP settings are passed through via `env_file: .env` (`required: false`), so absent is
  the only unset value. Copy `.env.example` to `.env` to enable real delivery; `.env` is gitignored.
  Quote any password containing `#` or spaces, or dotenv eats the rest of the line as a comment.
- **`spring.mail` has exactly eleven properties, and an unknown one is silently ignored.** Read
  from `spring-boot-mail`'s `spring-configuration-metadata.json`: `default-encoding`, `host`,
  `jndi-name`, `password`, `port`, `properties`, `protocol`, `ssl.bundle`, `ssl.enabled`,
  `test-connection`, `username`. There is **no** `spring.mail.starttls` and **no**
  `spring.mail.smtp.auth`. Both are traps: no error, no warning, no startup failure, the key is
  just dropped and the send fails later against a live relay. Auth and STARTTLS are the dotted map
  keys `spring.mail.properties.mail.smtp.auth` / `...starttls.enable`, which live in
  `application.properties` because an environment variable cannot express a dotted map key —
  `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH` does not bind. `ContactMailConfigurationTests` asserts
  all of this against the assembled `JavaMailSenderImpl`, so do not delete it to "simplify".

**Host port 8080 is not available on this machine.** The other project's `meminfo-rag-app`
container has host port 8080 *reserved but unbound* — it was started while the local
`mvnw spring-boot:run` held 8080, so its bind silently failed and
`NetworkSettings.Ports` is empty. Docker keeps the reservation, and `docker compose up` then fails
with `Bind for 0.0.0.0:8080 failed: port is already allocated`. Run with a different port:

```powershell
$env:PORT=8090; docker compose up -d
```

To reclaim 8080, change the published port in that other project's compose file and recreate it
(`docker compose up -d --force-recreate`) so its reservation is released.

## Encoding: no BOMs

Four files accidentally carried a UTF-8 BOM and the Windows `javac` accepted them while the Linux
`javac` inside the Docker build rejected them with `illegal character: '\ufeff'`. A BOM also risks
corrupting the first property in `application.properties`. Keep every source, resource, template,
and config file **BOM-free UTF-8**. If `docker compose build` fails on an illegal character that
`mvnw.cmd test` does not, suspect a BOM.

## Frontend: server-rendered Thymeleaf

`spring-boot-starter-thymeleaf` is the only view layer. `src/main/resources/templates/` holds
`index.html` plus `fragments/` (header, hero, about, skills, projects, contact, footer, and the
htmx `contact-result :: outcome` fragment); `src/main/resources/static/` holds `css/site.css` and
`img/`. Templates and fragments go in `templates/`, CSS/images in `static/`.
The hero photograph is `static/img/faleelimg.jpeg` (3:4, cropped into the square card with
`object-position: center 25%`); the abstract SVG placeholder that used to sit there is deleted and a
test asserts neither it nor the word "placeholder" returns.

**There is no custom JavaScript in this project, and that is a deliberate design constraint.**
`static/js/site.js` was deleted; the htmx CDN script in `fragments/layout.html` is the only `<script>`
on the page and exists solely to swap the contact result fragment in. Everything `site.js` used to do
is now server-rendered or pure CSS:

| Used to be JS | Is now |
| --- | --- |
| Project category filter | `?filter=<id>` → `PageController` → `PortfolioService.projectsFor`; pills are `<a>` |
| Mobile drawer button + class swap | `input#nav-toggle` checkbox + `<label>`, opened by `#nav-toggle:checked ~ .site-nav` |
| Active nav link via `IntersectionObserver` | `body:has(#section:target) .site-nav__link[data-section=…]` |
| `data-to-top` reveal + smooth scroll | plain `<a href="#top">` + `html { scroll-behavior: smooth }` |
| `navigator.clipboard` copy-email button | `mailto:` link |
| `data-placeholder-link` click-to-confess button | inert `<span>` + visually-hidden "not available yet" |
| Toast host | removed; `#contact-result` already announces via `role="status"` / `role="alert"` |
| Sticky-header `is-stuck` class | unconditional `box-shadow` |
| (never existed) dark/light toggle | `input#theme-toggle` + `body:has(#theme-toggle:checked)` token overrides |

**Dark mode is one block of token overrides, and that is only possible because every colour is a
token.** The light sheet originally carried 32 hardcoded hex values and 7 hardcoded `rgba()` calls
scattered through the component rules; those are now tokens (`--text-2`, `--border-strong`,
`--tint-success`, `--on-accent`, `--shadow-rgb`, …). Section 14 of `site.css` reassigns them under
`body:has(#theme-toggle:checked)`. If you introduce a literal colour in a component rule, dark mode
will silently not reach it — `theDarkThemeIsACheckboxAndEveryTokenItOverridesExistsInTheLightTheme`
asserts both directions: no literals in sections 2–13, and every token the dark block overrides must
actually be defined in `:root`. Note `--on-accent` inverts in dark mode (dark text on the now-lighter
purple fills), and text-on-accent contrast is asserted by hand, not by the test.

If you add behaviour, prefer a controller method, a model attribute, or a CSS selector. Before
reintroducing a `<script>`, note what is deliberately lost: the drawer no longer closes on Escape or
on an outside click (tap-outside is a full-viewport `<label>`, which does not cover keyboard), nav
highlighting follows navigation rather than scroll position, toasts are gone, and **the theme choice
resets to light on any full page load** — `?filter=` links and the contact form's Post/Redirect/Get
both reload, and there is no localStorage. That is the one real cost of keeping the no-JS rule.

**All page copy lives in `PortfolioService`, not in the templates.** It was rewritten with the
site owner's real details (Faleel H, Java developer, BCA graduate). `OWNER_NAME`, `OWNER_ROLE`,
`OWNER_EMAIL` and `OWNER_LOCATION` are the single source for the header, hero, footer, contact
card, favicon-ish mark, and `app.contact.to`. `noFictitiousIdentityIsLeftOnThePage` asserts none
of the old placeholder persona (Alex Rivera, `example.dev`, `99.99% Uptime`, the fake PGP key,
"Stanford") has crept back in — extend that test rather than re-introducing invented copy.

The only existing endpoint is `HelloController` (`src/main/java/com/example/demo/Controller/HelloController.java`):
`@RestController` returning a plain `String` from `GET /hello`. It is a placeholder, not a view
controller. Note the package segment is capitalized (`demo.Controller`); follow the existing
convention rather than lowercasing it. Indentation is already mixed (tabs in `DemoApplication`,
spaces in `HelloController`) and there is no formatter, so just stay consistent within a file.

The empty `<name/>`, `<description/>`, `<license/>`, `<developers/>`, `<scm/>` blocks in `pom.xml`
are deliberate overrides that stop the Spring Boot parent POM from injecting metadata (explained in
`HELP.md`). Leave them alone.

### Thymeleaf gotchas that already bit this project

1. **A `th:` attribute value is parsed as a single expression, and its tokenizer breaks on
   whitespace inside string literals.** `${a ? 'field__error' : 'field__error is-empty'}` fails
   with `Could not parse as expression: "error is-empty' : 'field"`. Put a single
   `class="..."` on the element and toggle a **one-word** modifier instead:
   `th:classappend="${cond ? 'is-error' : ''}"`. Same for `"${a} ? 'x' : 'y'"` — always wrap the
   whole ternary in `${}`.
2. **`th:hx-target="#contact-result"` does not work** — `#` starts a literal expression, so
   Thymeleaf tries to parse `#contact-result` and throws. htmx attributes go through one
   `th:attr` with single-quoted values:
   `th:attr="hx-post=@{/contact},hx-target='#contact-result',hx-swap='outerHTML'"`.
3. **`th:errors` deletes its host element when the field is valid.** An htmx out-of-band swap
   targeting that element then has nothing to replace, so the error disappears visually but the
   ID may not survive. Error slots must be permanent elements whose text and an `is-error` class
   are driven from a model map.
4. `Model` has no `addAttributeIfAbsent` (that is `ModelMap`). Use
   `model.asMap().containsKey(...)` if you need the check.
5. devtools recompiles with Eclipse JDT and surfaces Java errors as a runtime
   `java.lang.Error: Unresolved compilation problem`, not a build failure. If the live app starts
   but every request 500s, run `.\mvnw.cmd test` to get the real message.

## `Context/` is the spec, not code

`demo/Context/` holds the design/build specification for the portfolio site. Read it before
touching any UI. It is the source of truth for layout, copy, and design tokens.

- Sections are numbered and the labels are fixed strings: `01 / FOUNDATION` (About),
  `02 / CAPABILITIES` (Expertise), `03 / PORTFOLIO` (Projects), `04 / DIALOGUE` (Contact).
  `web_diagram.md` is the ASCII map of the whole page (Header / Hero / 01–04 / Footer).
- Each area has a pair: `*_Develop.md` says *what to build*, `*_Design.md` gives *pixel-exact*
  measurements (sizes, padding, radii, colors). When they disagree on a number, the `*_Design.md`
  file is the more precise one.
- The "half" / "other_half" naming is a file-size split of one prompt, not two different designs:
  `web_page_develop_prompt_half.md` (Header/Hero/About/Expertise) pairs with
  `web_page_design_prompt_half.md`, and `..._other_half.md` (Projects) pairs with
  `web_page_develop_prompt_other_half.md` + `web_page_design_prompt_other_half.md`.
- `extra_develop.md` / `extra_design.md` = Contact section; `footer_Develop.md` /
  `footer_Design.md` = footer. `Bootstrap_setup.md`, `Jquery_setup.md`, `htmx_reference.md` are
  just link references to third-party docs.

**Known conflict — resolve it this way:** `web_page_develop_prompt_half.md` and
`..._other_half.md` were written for a React/Next/Tailwind stack and say to rebuild with "React
components", SVG/Lucide icons, etc. This repo is Spring Boot + Thymeleaf. Use those files for
visual intent, layout, and exact copy only. Do **not** add a Node/npm/React/Tailwind build; map
"component" onto a Thymeleaf fragment/partial. Same for the CDN snippets in
`Bootstrap_setup.md` / `Jquery_setup.md` — they are reference links, not an instruction to vendor
those libraries.

## Design tokens — reuse, don't invent

From `web_page_develop_prompt_half.md` §16 and the design prompts:

| Role | Value |
| --- | --- |
| Primary purple | `#6D28D9` |
| Secondary purple | `#7C3AED` |
| Coral accent | `#C85A45` |
| Dark text | `#0B0F19` |
| Body text | `#334155` |
| Warm background | `#FAF9F6` |
| Card background | `#FFFFFF` |
| Border | `#E8E0D5` |
| Muted text | `#64748B` |
| Success | `#22C55E` |

- Content container: centered, **max-width ~845–850px**. Do not stretch to viewport.
- Fonts: modern geometric sans (Inter / Geist / Plus Jakarta Sans) for headings and body; a mono
  (JetBrains Mono / IBM Plex Mono) for the tiny uppercase technical labels and the letterspaced
  section numbers.
- Purple→coral gradient is reserved for primary CTAs and the highlighted hero phrase
  ("fluid digital products."). Not for whole headings, not for every badge.
- Borders over shadows; radii: buttons 20–24px, cards 14–16px, photo 12–14px, icon boxes 7–8px,
  tech pills 6px.
- Transitions 150–250ms, subtle hover lift only. Explicitly rejected: glassmorphism, maps,
  stock photos, decorative blobs, large illustrations, heavy shadows.

## Hard constraints from the spec

These are called out explicitly in the `Context/` files and are easy to violate accidentally:

- **Never flatten a section into a single background image.** Rebuild every element as real
  HTML/CSS/SVG. The only image asset is the hero photograph.
- The hero "Thiruvarur, India / Remote / Open to Work" panel is **UI overlaying the photo** — do
  not bake it into the image file.
- Never allow horizontal scrolling, text overlap, or buttons overflowing at mobile widths. The
  Core Concepts skills card spans ~2 grid columns on desktop and must collapse to one column on
  mobile.
- Both project cards must keep identical height/padding/radius despite differing description and
  feature-list lengths (`.project__features` is height-capped with internal scroll for this).
- Never invent metrics. The 01 / FOUNDATION stats are real (CGPA 7.58, 2 projects, 10+
  technologies, Java 21) and must stay that way. GitHub (`soloStack-Dev`), LinkedIn
  (`faleel-h-b772a1416`), and the two Source Repository links are real anchors. The Résumé is the
  only link with no destination: it renders as an inert `<span>` with visually-hidden
  "not available yet", so give it a real URL rather than deleting the element.
- Required + email validation, loading, success, and error states are all required on the
  contact form. `Subject` is the label for the `topic` field; keep the field name `topic` so the
  `err-topic` OOB swap target stays in sync. Validation settles on submit only — with no script
  listening for input events that is the native behaviour, and `.is-error` arrives from the
  server (Post/Redirect/Get render or htmx out-of-band swap).
- Accessibility is in scope: semantic HTML, heading hierarchy, keyboard nav, visible focus, alt
  text on the hero photo, icons never the sole carrier of an action, and reduced-motion support.
