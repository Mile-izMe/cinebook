FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/cinebook-0.0.1-SNAPSHOT.jar app.jar
ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx384m -XX:ActiveProcessorCount=2 -Duser.timezone=Asia/Ho_Chi_Minh"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
