# Stage 1: Build
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:25-jdk AS runtime
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
