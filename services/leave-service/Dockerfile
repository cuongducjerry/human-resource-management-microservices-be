# Stage 1: build jar bằng Gradle
FROM gradle:8.3-jdk17 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle clean build -x test

# Stage 2: chạy app
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/build/libs/leave-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java","-jar","app.jar"]