FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml .
COPY src ./src
RUN mvn -B package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/target/fraud-rule-engine-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
