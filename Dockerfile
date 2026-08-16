FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/tester3000-1.0.0-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
