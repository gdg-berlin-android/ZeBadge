FROM eclipse-temurin:17 AS build

WORKDIR /app
COPY . .
RUN apt-get update && apt-get install -y git 
RUN ./gradlew server:fatJar


FROM eclipse-temurin:17
WORKDIR /app
COPY --from=build /app/server/build/libs/server-1.0-SNAPSHOT-standalone.jar /app/server.jar
EXPOSE 1337

CMD ["java", "-jar", "/app/server.jar"]
