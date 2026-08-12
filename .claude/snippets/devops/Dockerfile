# snippets/devops/Dockerfile
# Multi-stage build con usuario no privilegiado y healthcheck — ver recipes/deploy-docker-application.md.

FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
USER app
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -q -O- http://localhost:8080/health || exit 1
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
