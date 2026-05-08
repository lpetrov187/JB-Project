# ── Stage 1: build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# Copy Gradle wrapper first — layer is cached as long as these don't change
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .
COPY settings.gradle .

# Download dependencies (cached layer, only re-runs if build.gradle changes)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# Copy source and build the self-contained JAR
COPY src/ src/
RUN ./gradlew jar --no-daemon

# ── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/jbl-1.0.0.jar jbl.jar
ENTRYPOINT ["java", "-jar", "jbl.jar"]
