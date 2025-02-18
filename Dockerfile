FROM openjdk:17-jdk-slim

WORKDIR /app
COPY target/diploma-cloudservice-0.0.1-SNAPSHOT.jar cloudService.jar

EXPOSE 8080
CMD ["java", "-jar", "cloudService.jar"]