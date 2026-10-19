FROM eclipse-temurin:17-jre-alpine
WORKDIR /application
COPY app/target/ROOT.war app.war
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.war"]