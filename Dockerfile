# 1. Use Maven to build the app
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# Package the application
RUN mvn clean package -DskipTests

# 2. Use lightweight JDK image to run the app
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy the built jar from the previous stage
COPY --from=build /app/target/genAI_coding_challenge_anurag-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
