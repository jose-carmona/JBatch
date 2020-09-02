#!/bin/sh
cp $(pwd)/target/jbatch.war $(pwd)/deploy/
docker run --rm -p 8080:8080 -p 4848:4848 -v $(pwd)/deploy:/opt/payara/deployments --name jbatch payara/server-full:latest