FROM eclipse-temurin:21-jdk-alpine AS build
RUN apk add maven
WORKDIR /app
COPY pom.xml pom.xml
COPY src/ src/
RUN --mount=type=cache,target=/root/.m2 mvn package -DskipTests

FROM eclipse-temurin:21-jdk-alpine
COPY --from=build /app/target/awesomemodeling-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
