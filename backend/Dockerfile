# ------------------------
# Stage 1: Build the app
# ------------------------
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Set work directory
WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src ./src

# Build the project
RUN mvn clean package -DskipTests

# ------------------------
# Stage 2: Run the app
# ------------------------
FROM eclipse-temurin:21-jdk-alpine

# Set work directory
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Expose port (Spring Boot default)
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
