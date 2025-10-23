FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="iFood Clone Team"

VOLUME /tmp

COPY target/*.jar app.jar

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "/app.jar"]