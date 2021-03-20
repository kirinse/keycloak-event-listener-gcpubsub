# keycloak-event-listener-gcpubsub

##### A Keycloak SPI plugin that publishes events to Google Cloud Pub/Sub.

For example here is the notification of the user updated by administrator

* attributes:

```
eventType: "ADMIN"
realmId: "MYREALM"
resourceType: "USER"
operationType: "UPDATE"
```
* content: 

```
{
  "@class" : "com.github.acesso_io.keycloak.event.provider.EventAdminNotificationGcpsMsg",
  "time" : 1596951200408,
  "realmId" : "MYREALM",
  "authDetails" : {
    "realmId" : "master",
    "clientId" : "********-****-****-****-**********",
    "userId" : "********-****-****-****-**********",
    "ipAddress" : "192.168.1.1"
  },
  "resourceType" : "USER",
  "operationType" : "UPDATE",
  "resourcePath" : "users/********-****-****-****-**********",
  "representation" : "representation details here....",
  "error" : null,
  "resourceTypeAsString" : "USER"
}
```

## USAGE:
1. [Download the latest jar](https://github.com/acesso-io/keycloak-event-listener-gcpubsub/blob/target/keycloak-to-gcpubsub-1.0.jar?raw=true) or build from source: ``mvn clean install``
2. copy jar into your Keycloak `/opt/jboss/keycloak/standalone/deployments/keycloak-to-gcpubsub-1.0.jar`
3. Configure as described below (option 1 or 2 or 3)
4. Restart the Keycloak server
5. Enable logging in Keycloak UI by adding **keycloak-to-gcpubsub**  
 `Manage > Events > Config > Events Config > Event Listeners`

#### Configuration 

#### Set `GOOGLE_APPLICATION_CREDENTIALS` environment variable pointing to your service account credential file

###### OPTION 1: just configure **ENVIRONMENT VARIABLES**
  - `KC_TO_GCP_PROJECTID` - default: *identity-test*
  - `KC_TO_GCP_EVENTTOPICID` - default: *keycloak-events*
  - `KC_TO_GCP_ADMINEVENTTOPICID` - default: *keycloak-events*

###### OPTION 2: edit Keycloak subsystem of WildFly standalone.xml or standalone-ha.xml:

```xml
<spi name="eventsListener">
    <provider name="keycloak-to-gcpubsub" enabled="true">
        <properties>
            <property name="projectId" value="${env.KC_TO_GCP_PROJECTID:identity-test}"/>
            <property name="eventTopicId" value="${env.KC_TO_GCP_EVENTTOPICID:keycloak-events}"/>
            <property name="adminEventTopicId" value="${env.KC_TO_GCP_ADMINEVENTTOPICID:keycloak-events}"/>
        </properties>
    </provider>
</spi>
```
###### OPTION 3: same effect as OPTION 2 but programatically:
```
echo "yes" | $KEYCLOAK_HOME/bin/jboss-cli.sh --file=$KEYCLOAK_HOME/KEYCLOAK_TO_GCPUBSUB.cli
```


