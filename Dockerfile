# 빌드용
FROM gradle:8.14.3-jdk17 AS build

WORKDIR /app

COPY build.gradle settings.gradle ./
RUN gradle --version

COPY src ./src

RUN gradle build -x test

# 실행 용
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/build/libs/*.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]