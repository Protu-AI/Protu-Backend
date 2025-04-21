FROM openjdk:17-jdk-alpine AS build

WORKDIR /content-service

COPY mvnw pom.xml ./
COPY .mvn ./.mvn

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B -T 1C

COPY src ./src

RUN ./mvnw package -DskipTests -T 1C

FROM openjdk:17-jdk-alpine

WORKDIR /content-service

COPY --from=build content-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]