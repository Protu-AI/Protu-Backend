FROM eclipse-temurin:23-jdk-alpine AS build

WORKDIR /user-service

COPY mvnw pom.xml ./
COPY .mvn ./.mvn

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B -T 1C

COPY src ./src

RUN ./mvnw package -DskipTests -T 1C

FROM eclipse-temurin:23-jre-alpine

WORKDIR /user-service

COPY --from=build user-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]