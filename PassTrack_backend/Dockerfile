# Stage 1: Build with Maven and JDK 21
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime with JRE 21 Alpine (non-root)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S passtrack && adduser -S passtrack -G passtrack \
    && mkdir -p /app/uploads \
    && chown -R passtrack:passtrack /app

COPY --from=builder --chown=passtrack:passtrack /app/target/*.jar app.jar

USER passtrack

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
