FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY order-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-jar", "app.jar"]