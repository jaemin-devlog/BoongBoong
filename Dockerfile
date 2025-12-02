# Note: Avoid BuildKit-only syntax directive for broader compatibility
# Build stage
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Leverage layer caching: copy only build descriptors first
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

# Optionally warm dependency cache (non-fatal if offline)
RUN ./gradlew --no-daemon --build-cache --configuration-cache -Dorg.gradle.parallel=true dependencies || true

# Copy sources and build the bootable jar
COPY src src
RUN ./gradlew --no-daemon --build-cache --configuration-cache -Dorg.gradle.parallel=true clean bootJar -x test

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the fat jar from the build stage
COPY --from=build /app/build/libs/*.jar /app/app.jar

# The app listens on 8080 (Render maps externally)
EXPOSE 8080

# Common JVM options; override with -e JAVA_OPTS="..." when running
ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom -XX:MaxRAMPercentage=75 -XX:+UseSerialGC"

# Start the application
ENTRYPOINT ["java","-jar","/app/app.jar"]
