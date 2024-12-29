# multistage docker

FROM openjdk:22-jdk-oracle AS builder

ARG COMPILE_DIR=/compiledir

WORKDIR ${COMPILE_DIR}

COPY mvnw .
COPY mvnw.cmd .
COPY pom.xml .
COPY .mvn .mvn
COPY src src

#run command
RUN chmod a+x ./mvnw && ./mvnw package -Dmaven.test.skip=true

FROM openjdk:22-jdk-oracle

ARG WORK_DIR=/app

WORKDIR ${WORK_DIR}

# from builder [dir for file in builder] [new filename for this stage]
COPY --from=builder /compiledir/target/instrumentrentalapp-0.0.1-SNAPSHOT.jar instrumentrentalapp.jar

# check if curl command avail and install if needed - this is for healthcheck
# RUN apt update && apt install -y curl

ENV SERVER_PORT=8080

ENV SPRING_DATA_REDIS_HOST=localhost
ENV SPRING_DATA_REDIS_PORT=6379
ENV SPRING_DATA_REDIS_DATABASE=0
ENV SPRING_DATA_REDIS_USERNAME=""
ENV SPRING_DATA_REDIS_PASSWORD=""

# public api urls
ENV GOOGLE_STATICMAP_API_URL=https://maps.googleapis.com/maps/api/staticmap
ENV GOOGLE_GEOCODING_API_URL=https://maps.googleapis.com/maps/api/geocode/json

EXPOSE ${SERVER_PORT}

SHELL [ "/bin/sh", "-c" ]

ENTRYPOINT SERVER_PORT=${SERVER_PORT} java -jar instrumentrentalapp.jar

# health check
HEALTHCHECK --interval=120s --timeout=30s --start-period=60s --retries=3 CMD curl -s -f http:/localhost:${SERVER_PORT}/health || exit 1