FROM amazoncorretto:21-alpine

ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8081

WORKDIR /app

COPY target/*.jar app.jar
#COPY src/main/resources/application.yml application.yml

EXPOSE 8080 8081

ENTRYPOINT ["java", "-jar", "app.jar"]