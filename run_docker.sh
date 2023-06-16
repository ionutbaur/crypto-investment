#!/bin/bash

#build new jar
./mvnw clean install

#build image locally
docker build --tag=crypto-investment:latest .

#run the container on port 8080
docker run --name="crypto-investment" -p8080:8080 crypto-investment