FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY gateway-service/target/*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-jar", "app.jar"]