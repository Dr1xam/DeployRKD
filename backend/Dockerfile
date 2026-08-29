# ==========================================
# Stage 1: Build stage
# ==========================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper and configuration
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Ensure wrapper script is executable and resolve dependencies
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B || true

# Copy application source code
COPY src/ src/

# Build Spring Boot executable JAR
RUN ./mvnw clean package -DskipTests

# ==========================================
# Stage 2: Runtime stage
# ==========================================
FROM eclipse-temurin:17-jre-alpine AS runner

WORKDIR /app

# Create a non-privileged user and group
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Set file permissions
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Configure application port (5000 as required)
ENV SERVER_PORT=5000
EXPOSE 5000

# Execute Spring Boot application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
