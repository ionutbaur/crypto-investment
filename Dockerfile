FROM openjdk:11-jdk

MAINTAINER ionutzBaur

RUN mkdir -p "src/main/resources/prices"
COPY src/main/resources/ src/main/resources/
COPY target/crypto-investment-0.0.1-SNAPSHOT.jar crypto-investment.jar

ENTRYPOINT ["java","-jar","/crypto-investment.jar"]