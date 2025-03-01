FROM openjdk:17-jdk-alpine AS build

WORKDIR /service-discovery

COPY mvnw pom.xml ./
COPY .mvn ./.mvn

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B -T 1C

COPY src ./src

RUN ./mvnw package -DskipTests -T 1C

FROM openjdk:17-jdk-alpine

WORKDIR /service-discovery

COPY --from=build service-discovery/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]