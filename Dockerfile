# build
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

COPY pom.xml .

RUN --mount=type=cache,target=/root/.m2 \
  mvn dependency:go-offline -B --quiet || true

COPY src/ src/
RUN --mount=type=cache,target=/root/.m2 \
  mvn package -DskipTests -B --quiet

# runtime
FROM eclipse-temurin:17-jre-alpine AS runtime

RUN addgroup -S workhub && adduser -S workhub -G workhub
USER workhub

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
