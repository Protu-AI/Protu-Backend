FROM eclipse-temurin:23-jdk-alpine AS build

WORKDIR /notification-service

COPY mvnw pom.xml ./
COPY .mvn ./.mvn

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B -T 1C

COPY src ./src

RUN ./mvnw package -DskipTests -T 1C

FROM eclipse-temurin:23-jre-alpine

WORKDIR /notification-service

COPY --from=build notification-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]