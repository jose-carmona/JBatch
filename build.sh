#!/bin/sh
docker run -it --rm --name my-maven-project -v "$(pwd)":/usr/src/mymaven -v "$(pwd)/.m2":/root/.m2 -w /usr/src/mymaven maven:3-jdk-8 mvn clean package