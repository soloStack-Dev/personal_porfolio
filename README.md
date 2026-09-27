# personal_porfolio

Server-rendered personal portfolio site for **Faleel H** — Java Developer.

Single Spring Boot application, no client-side framework. The browser receives finished HTML;
htmx is used only to enhance the contact form so it submits without a full page reload, and the
form still works with JavaScript disabled.

## Stack

| | |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 4.1.1 (WebMvc, Thymeleaf, Validation, Mail, Actuator) |
| View layer | Thymeleaf templates + fragments, server-rendered |
| Frontend | Hand-written CSS, vanilla JS, htmx 2, inline SVG icon set |
| Build | Maven 3.9.16 (via the bundled wrapper) |
| Deploy | Docker (multi-stage) or the runnable jar |

There is no Node, npm, bundler, or CSS framework in this repository. Every icon is an inline SVG
drawn on a 24x24 grid; the only image asset is the hero photograph.

## Sections

Header, hero, `01 / FOUNDATION` (About), `02 / CAPABILITIES` (Expertise), `03 / PORTFOLIO`
(Projects), `04 / DIALOGUE` (Contact), and a footer.

## Running it

### Locally

```powershell
cd demo
.\mvnw.cmd spring-boot:run
```

Then open <http://localhost:8080>. DevTools is on the classpath, so editing a template or a class
triggers a restart.

### Tests and jar

```powershell
cd demo
.\mvnw.cmd test        # 19 tests: PortfolioPageTests (18) + DemoApplicationTests (1)
.\mvnw.cmd package     # target\demo-0.0.1-SNAPSHOT.jar
```

### Docker

```powershell
cd demo
docker compose up -d --build
docker compose ps          # STATUS reaches (healthy) after the first check
docker compose logs -f
docker compose down
```

The host port comes from `PORT` (default `8080`); the container always listens on `8080`.

The image is a two-stage build: `maven:3.9.16-eclipse-temurin-21` compiles and runs the tests,
`eclipse-temurin:21-jre-jammy` runs the resulting jar as uid 65534. The healthcheck requests `/`
rather than `/actuator/health`, because the actuator aggregate includes `MailHealthIndicator`,
which fails when no SMTP relay is configured — the default state — and would report the container
down while the site is serving normally.

## Contact form

The form works with no configuration. With no `spring.mail.host`, no mail sender is created and
`ContactMailService` logs the enquiry instead of sending it, and the page says so honestly rather
than claiming delivery.

To route submissions to a real mailbox, copy `demo/.env.example` to `demo/.env`, fill in the SMTP
values, and recreate the container. `.env` is gitignored.

## Layout

```
demo/
  Dockerfile                  multi-stage build
  docker-compose.yml          single service, no database
  .env.example                template for optional SMTP settings
  src/main/java/com/example/demo/
    Controller/               page + contact endpoints
    Model/                    ContactForm, Project, ProjectLink, ContactDelivery
    Service/                  PortfolioService (all page copy), ContactMailService
  src/main/resources/
    templates/                index.html + fragments/
    static/                   css/, js/, img/
  src/test/java/...           MockMvc rendering and form tests
AGENTS.md                     repo conventions and the design spec's constraints
```

`Context/` holds the design specification the UI was built from: layout, exact measurements,
copy, and design tokens. `AGENTS.md` records the conventions and the traps this codebase has
already hit.

## Note on credentials

`demo/.env` is gitignored and must stay that way — it holds the SMTP password. Only
`.env.example`, which contains no values, belongs in the repository.
