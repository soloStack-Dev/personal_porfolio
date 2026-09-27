<p align="center">
  <img src="./assets/header.svg" alt="Faleel H — Java Developer" width="880">
</p>

<p align="center">
  Backend work in Java and Spring Boot — REST services, data layers, and the unglamorous
  parts in between.
</p>

<p align="center">
  <img alt="java" src="https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white">
  <img alt="spring boot" src="https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?style=flat-square&logo=springboot&logoColor=white">
  <img alt="thymeleaf" src="https://img.shields.io/badge/Thymeleaf-005C0E?style=flat-square&logo=thymeleaf&logoColor=white">
  <img alt="docker" src="https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white">
  <img alt="mysql" src="https://img.shields.io/badge/MySQL-8-4479A1?style=flat-square&logo=mysql&logoColor=white">
  <img alt="tests" src="https://img.shields.io/badge/tests-19%20passing-22c55e?style=flat-square">
</p>

---

## What this is

My portfolio site, built as a single Spring Boot application. The server sends finished HTML —
there is no client-side framework and no build step for the frontend. htmx is used for exactly
one thing: letting the contact form submit without a full page reload. Turn JavaScript off and
the form still works, because it is a plain `POST` with `Post/Redirect/Get`.

The interesting constraint was keeping it server-rendered while still feeling like a
2020s frontend. That meant real CSS for the sticky header, the mobile drawer, the hover states,
and the reduced-motion fallbacks, rather than a framework doing it for me.

## Stack

| Layer | Choice |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 4.1.1 — WebMvc, Thymeleaf, Validation, Mail, Actuator |
| Views | Thymeleaf templates and fragments, server-rendered |
| Frontend | Hand-written CSS, vanilla JS, htmx 2, inline SVG icons |
| Build | Maven 3.9.16 via the bundled wrapper |
| Packaging | Multi-stage Docker image, or the runnable jar |
| Database | MySQL 8, on the deployed projects |

No Node, no npm, no bundler, no CSS framework. Every icon is an inline SVG on a 24×24 grid, and
the only image in the repository is the photograph in the hero.

## Sections

Header · hero · `01 / FOUNDATION` (About) · `02 / CAPABILITIES` (Expertise) ·
`03 / PORTFOLIO` (Projects) · `04 / DIALOGUE` (Contact) · footer

## Run it

Locally, with the wrapper:

```powershell
cd demo
.\mvnw.cmd spring-boot:run        # http://localhost:8080
```

Or with Docker:

```powershell
cd demo
docker compose up -d --build
docker compose ps                 # (healthy) once the first check passes
docker compose logs -f
docker compose down
```

`PORT` chooses the host port; the container always listens on `8080`.

Run the tests:

```powershell
cd demo
.\mvnw.cmd test                   # 19 tests
```

## A few things I had to get right

**The healthcheck does not use `/actuator/health`.** Actuator aggregates every health indicator,
and `MailHealthIndicator` throws when no SMTP relay is configured — which is the default state.
The aggregate reports the container down while the site is serving perfectly. The healthcheck
requests `/` instead, which checks the thing that matters: that the page renders.

**The contact form is honest when it cannot send.** With no `spring.mail.host`, no mail sender is
created and the service logs the enquiry rather than pretending to deliver it, and the page says
so. Copy `demo/.env.example` to `demo/.env` and fill in the SMTP values to send for real.

**`th:replace` silently deletes the element it is on.** Every icon include originally used it,
which discarded the wrapper's `class` and `data-*` attributes. The visible symptom was two
glyphs stuck in the mobile menu button, and the deeper one was that the JavaScript could not find
the icon hooks at all, so the hamburger never became a cross. The fix was `th:insert`, and there
is now a regression test for it.

**A UTF-8 BOM builds on Windows and fails on Linux.** Four files carried one. Windows `javac`
accepted them; the `javac` inside the Docker build rejected them. Worth knowing if a CI build
fails on a file that works locally.

## Layout

```
demo/
  Dockerfile                  multi-stage build
  docker-compose.yml          one service, no database
  .env.example                template for the optional SMTP settings
  src/main/java/com/example/demo/
    Controller/               page and contact endpoints
    Model/                    ContactForm, Project, ProjectLink, ContactDelivery
    Service/                  PortfolioService (all page copy), ContactMailService
  src/main/resources/
    templates/                index.html and fragments/
    static/                   css/, js/, img/
  src/test/java/…             MockMvc rendering and form tests
demo/Context/                 the design specification the UI was built from
AGENTS.md                     repo conventions, and the traps this codebase has already hit
```

Page copy lives in `PortfolioService`, not in the templates, so the text is in one reviewable
place instead of scattered across fragments.

## Contact

[![GitHub](https://img.shields.io/badge/GitHub-soloStack--Dev-181717?style=flat-square&logo=github&logoColor=white)](https://github.com/soloStack-Dev)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-faleel--h-0A66C2?style=flat-square&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/faleel-h-b772a1416)
[![Email](https://img.shields.io/badge/Email-faleelmr4@gmail.com-c85a45?style=flat-square&logo=gmail&logoColor=white)](mailto:faleelmr4@gmail.com)

## License

© Faleel H. All rights reserved.
