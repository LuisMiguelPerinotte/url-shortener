# syntax=docker/dockerfile:1

# Build stage: resolves dependencies first so they are cached while only src changes.
# Tests are not run here; they run in CI (./mvnw -B verify).
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q -DskipTests package

# Runtime stage: JRE only, non-root user. The *.jar glob skips the *.jar.original
# file that the Spring Boot repackage goal leaves in target/.
FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
