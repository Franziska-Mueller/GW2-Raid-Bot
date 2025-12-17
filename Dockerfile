# --- Artifact Build Stage --- #
FROM maven:3-amazoncorretto-21 as builder

RUN mkdir /app
WORKDIR /app
COPY . /app

RUN mvn package

# --- Image Build Stage --- #
FROM eclipse-temurin:21

WORKDIR /opt/app

RUN mkdir -p /opt/app/logs
RUN mkdir -p /opt/app/db

VOLUME [ "/opt/app/db", "/opt/app/logs" ]

COPY --from=builder /app/target/GW2-Raid-Bot-*.jar /opt/app/GW2-Raid-Bot.jar

CMD ["java", "-jar", "./GW2-Raid-Bot.jar"]
