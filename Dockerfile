FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/swiftpay-ledger-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
