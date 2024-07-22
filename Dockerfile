FROM openjdk:17-jdk

WORKDIR /app

COPY target/vacations-api-1-zpwlqj-1.0.jar /app/vacation-tracker.jar

EXPOSE 8080

CMD ["java", "-jar", "vacation-tracker.jar"]