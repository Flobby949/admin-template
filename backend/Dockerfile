# Multi-stage build with cached dependencies for faster rebuilds.
# This Dockerfile is module-agnostic and will automatically include all Maven modules.
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy entire project structure (excluding files in .dockerignore)
# Maven will automatically detect all modules from parent POM
COPY . .

# Download dependencies and build in one step
# The BuildKit cache mount ensures dependencies are cached even if source changes
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl admin-boot -am -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

# Install curl for container health checks.
RUN apt-get update \
  && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/*

# Create a non-root user and log directory expected by logback.
RUN useradd -r -u 10001 appuser \
  && mkdir -p /opt/logs/admin-api \
  && chown -R appuser:appuser /opt/logs/admin-api

COPY --from=build /app/admin-boot/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75" \
    SPRING_PROFILES_ACTIVE=prod

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health | grep -q '\"status\":\"UP\"' || exit 1

USER appuser

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
