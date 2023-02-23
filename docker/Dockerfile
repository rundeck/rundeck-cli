FROM eclipse-temurin:17-jre-alpine

RUN mkdir -p /app
COPY build/app/rd-cli.jar /app

ENV RD_AUTH_PROMPT false

ENTRYPOINT ["java", "-jar", "/app/rd-cli.jar"]
CMD ["--help"]