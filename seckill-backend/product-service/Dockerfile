FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY product-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-jar", "app.jar"]