FROM payara/server-full:latest
COPY target/jbatch.war $DEPLOY_DIR