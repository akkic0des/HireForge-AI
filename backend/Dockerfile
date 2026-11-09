FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY backend/pom.xml ./pom.xml
COPY backend/src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/hireforge-backend-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 10000
ENTRYPOINT ["java", "-XX:+UseSerialGC", "-XX:MaxRAMPercentage=45.0", "-XX:MaxMetaspaceSize=128m", "-XX:ReservedCodeCacheSize=32m", "-XX:MaxDirectMemorySize=32m", "-Xss512k", "-jar", "/app/app.jar"]
