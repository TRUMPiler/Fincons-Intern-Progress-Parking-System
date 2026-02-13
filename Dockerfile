# Stage 1: Build the application using a Java 21 base image
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven project file to leverage Docker layer caching
# This downloads dependencies only when pom.xml changes
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the application source code
COPY src ./src

# Build the application, skipping tests as they are not needed for the final image
RUN mvn clean install -DskipTests


# Stage 2: Create the final, lightweight production image
FROM eclipse-temurin:21-jre-jammy

# Create a non-root user for security
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

# Set the working directory
WORKDIR /app

# Copy the executable JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Set the PORT environment variable. Render will use this to expose the service.
ENV PORT=8080

# Expose the port
EXPOSE 8080

# Command to run the application
# The exec form is used to ensure proper signal handling
ENTRYPOINT ["java", "-jar", "app.jar"]
