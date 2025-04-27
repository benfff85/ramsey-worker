# Use a Maven base image for building the application
FROM maven:3.9-eclipse-temurin-24 AS build

# Set the working directory
WORKDIR /app

# Copy only the Maven build file for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code after caching dependencies
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Use a smaller JRE image for runtime
FROM eclipse-temurin:24-jre AS final

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/ramsey-worker-*.jar /app/ramsey-worker.jar

# JVM memory and container awareness settings
ENV JAVA_OPTS="-Xms2g -Xmx2g -XX:MaxRAMPercentage=75.0"

# Add a non-root user and switch to it
RUN groupadd -r appgroup && useradd -r -g appgroup appuser
USER appuser

# Specify the command to run the application with JAVA_OPTS from the environment
CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/ramsey-worker.jar"]
