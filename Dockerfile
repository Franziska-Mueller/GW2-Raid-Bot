FROM maven:3-jdk-10
WORKDIR /data
COPY . /data
#RUN mvn -B -f /data/pom.xml -Ddetail=true clean package
#RUN mvn -U -X -f /data/pom.xml -Ddetail=true clean package
#RUN mvn -U -X -f /data/dependency-reduced-pom.xml -Ddetail=true clean package
RUN mvn -X -f /data/pom.xml clean compile assembly:single

FROM openjdk:10-jre-slim
WORKDIR /data
COPY Reactions.zip start LICENSE README.md /data/
COPY --from=0 /data/target/GW2-Raid-Bot-1.0-SNAPSHOT-jar-with-dependencies.jar /data/bot.jar
RUN chmod +x /data/start
CMD ["/data/start"]