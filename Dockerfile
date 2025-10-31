FROM gradle:8.14.3-jdk17

WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

RUN chmod +x ./gradlew

# src는 volume로 마운트할 거라 복사 안 함
EXPOSE 8080

CMD ["./gradlew", "bootRun"]