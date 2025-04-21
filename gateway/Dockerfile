FROM openjdk:17-jdk-alpine AS build

WORKDIR /gateway

COPY mvnw pom.xml ./
COPY .mvn ./.mvn

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B -T 1C

COPY src ./src

RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jdk-alpine

WORKDIR /gateway

COPY --from=build /gateway/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]