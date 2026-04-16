# Multi-stage build for Book Service
FROM maven:3.8.7-eclipse-temurin-17 AS build

WORKDIR /app

# Copy Book Service files
COPY . .

# Build the application with spring-boot-starter-parent
RUN mvn clean package -DskipTests -Dspring-boot.repackage.skip=false

# Runtime stage
FROM eclipse-temurin:17.0.8.1_1-jre

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r bookstore && useradd -r -g bookstore bookstore

# Copy the built JAR
COPY --from=build /app/target/book-service-1.0.0.jar app.jar

# Change ownership to non-root user
RUN chown -R bookstore:bookstore /app

USER bookstore

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
