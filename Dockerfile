FROM openjdk:21-jdk

LABEL org.opencontainers.image.authors="Ionut Baur"

RUN mkdir -p "src/main/resources/prices"
COPY src/main/resources/ src/main/resources/
COPY target/crypto-investment-0.0.2-SNAPSHOT.jar crypto-investment.jar

ENTRYPOINT ["java","-jar","/crypto-investment.jar"]