# syntax=docker/dockerfile:1
#
# Multi-stage build: Maven compiles and tests, a JRE-only image runs the result.
# Nothing from the build stage leaks into the runtime image, so the shipped layer holds
# a JRE and one jar instead of a JDK, Maven, and a local ~/.m2 repository.

# ---------------------------------------------------------------------------
# 1. Build
# ---------------------------------------------------------------------------
FROM maven:3.9.16-eclipse-temurin-21 AS build

WORKDIR /build

# Dependencies first, and on their own layer: this only re-resolves when pom.xml changes,
# so an ordinary source edit reuses the cached ~/.m2 from the previous build.
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B clean package

# ---------------------------------------------------------------------------
# 2. Runtime
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy AS runtime

# curl is only here for the container HEALTHCHECK below; drop both if you never call
# `docker inspect` for health and want a smaller image.
RUN apt-get update \
    && apt-get install --no-install-recommends -y curl \
    && rm -rf /var/lib/apt/lists/*

# Run as an unprivileged user. The base image already ships `nobody` (uid 65534), which is
# enough — no home directory, no shell, no write access to the app.
WORKDIR /app
RUN chown 65534:65534 /app

COPY --from=build --chown=65534:65534 \
     /build/target/demo-*.jar /app/app.jar

USER 65534:65534

EXPOSE 8080

# Heap is sized from the container's memory limit rather than the host's, and the
# percent-of-RAM ceiling keeps a large -Xmx from ever being OOM-killed by the cgroup.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8"

# Deliberately NOT /actuator/health. That endpoint aggregates every health indicator, and
# MailHealthIndicator throws when no SMTP relay is configured — which is the default state
# here — so it would report the container DOWN while the site serves requests perfectly
# well. Requesting `/` instead checks the thing that actually matters: that Thymeleaf
# renders the page. Actuator is still exposed if you want it by hand.
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
    CMD curl -fsS -o /dev/null http://localhost:8080/ || exit 1

# exec form: java is PID 1 and receives SIGTERM directly, so `docker stop` / compose down
# shut the JVM down instead of waiting out the 10s kill timeout.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
