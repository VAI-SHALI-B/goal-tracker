FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

ARG JAR_FILE=target/goal-tracker.jar
COPY ${JAR_FILE} app.jar

ENV PORT=5000
ENV APP_VERSION=v2
EXPOSE 5000

ENTRYPOINT ["java", "-jar", "app.jar"]
