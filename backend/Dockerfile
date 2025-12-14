# syntax=docker/dockerfile:1

# Auto generated from docker init

################################################################################

# Create a stage for resolving and downloading dependencies.
FROM maven:3.9.6-eclipse-temurin-21 AS deps

WORKDIR /app

# Download dependencies as a separate step to take advantage of Docker's caching.
# Leverage a cache mount to /root/.m2 so that subsequent builds don't have to
# re-download packages.
RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -DskipTests

################################################################################

FROM deps AS package

WORKDIR /app

COPY ./src src/
RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests && \
    mv target/$(mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout)-$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout).jar target/app.jar

################################################################################

FROM package AS extract

WORKDIR /app

RUN java -Djarmode=layertools -jar target/app.jar extract --destination target/extracted

################################################################################

# Create a development stage with hot-reload capabilities
FROM maven:3.9.6-eclipse-temurin-21 AS dev

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -DskipTests

# Copy source code
COPY ./src src/

# Expose application and debug ports
EXPOSE 8080 5005

# Build the application skipping tests, then run it
CMD ["sh", "-c", "mvn clean package -DskipTests && mvn spring-boot:run -DskipTests"]

################################################################################

FROM eclipse-temurin:21-jre-jammy AS final

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create a non-privileged user that the app will run under.
# See https://docs.docker.com/go/dockerfile-user-best-practices/
ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser
USER appuser

# Copy the executable from the "package" stage.
COPY --from=extract /app/target/extracted/dependencies/ ./
COPY --from=extract /app/target/extracted/spring-boot-loader/ ./
COPY --from=extract /app/target/extracted/snapshot-dependencies/ ./
COPY --from=extract /app/target/extracted/application/ ./

EXPOSE 8080

ENTRYPOINT [ "java", "org.springframework.boot.loader.launch.JarLauncher" ]
