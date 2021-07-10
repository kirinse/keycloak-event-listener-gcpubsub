FROM maven:3.6-openjdk-11 as buildereventer

COPY    ./pom.xml  /tmp/
COPY    ./src      /tmp/src/

WORKDIR /tmp/

RUN     mvn package
FROM jboss/keycloak:12.0.4

COPY --from=buildereventer  /tmp/target/keycloak-to-gcpubsub-1.0.1.jar    /opt/jboss/keycloak/standalone/deployments/
COPY ./svc_account.secret /opt/svc_account.secret
