# ---- Build Stage ----
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy only necessary files for dependency caching first
COPY . .

# Build the JAR
RUN gradle clean build -x test

# ---- Runtime Stage ----
FROM eclipse-temurin:17-jdk
RUN rm -rf /usr/local/tomcat/webapps/*
RUN mkdir -p /logs

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/api-gateway-1.0-SNAPSHOT.jar /usr/local/tomcat/webapps/API-Gateway-0.0.1-SNAPSHOT.jar

EXPOSE 8080
CMD ["sh", "-c", "java -jar /usr/local/tomcat/webapps/API-Gateway-0.0.1-SNAPSHOT.jar"]
