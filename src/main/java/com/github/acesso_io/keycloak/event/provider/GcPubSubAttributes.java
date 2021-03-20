package com.github.acesso_io.keycloak.event.provider;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
@JsonTypeInfo(use = Id.CLASS)
public class GcPubSubAttributes implements Serializable {
	
	private static final long serialVersionUID = 2818331369850992957L;

	private String eventType;
	private String realmId;
	private String errorStatus;
	private String resourceType;
    private String operationType;
    private String clientId;
    private String eventId;

	public static GcPubSubAttributes create(AdminEvent adminEvent) {

		GcPubSubAttributes attr = new GcPubSubAttributes();
		attr.eventType = "ADMIN";
		attr.realmId = adminEvent.getRealmId();
		attr.errorStatus = (adminEvent.getError() != null ? "ERROR" : "SUCCESS");
		attr.resourceType = adminEvent.getResourceTypeAsString();
		attr.operationType = adminEvent.getOperationType().toString();
		return attr;
	}
	
	public static GcPubSubAttributes create(Event event) {

		GcPubSubAttributes attr = new GcPubSubAttributes();
		attr.eventType = "CLIENT";
		attr.realmId = event.getRealmId();
		attr.errorStatus = (event.getError() != null ? "ERROR" : "SUCCESS");
		attr.clientId = event.getClientId();
		attr.eventId = event.getType().toString();
		return attr;
	}

	public static Map<String, String> createMap(AdminEvent adminEvent) {

		Map<String, String> map = new HashMap<String, String>();

		map.put("eventType", "ADMIN");
		map.put("realmId", adminEvent.getRealmId());
		map.put("errorStatus", (adminEvent.getError() != null ? "ERROR" : "SUCCESS"));
		map.put("resourceType", adminEvent.getResourceTypeAsString());
		map.put("operationType", adminEvent.getOperationType().toString());
		return map;
	}
	
	public static Map<String, String> createMap(Event event) {

		Map<String, String> map = new HashMap<String, String>();

		map.put("eventType","CLIENT");
		map.put("realmId", event.getRealmId());
		map.put("errorStatus", (event.getError() != null ? "ERROR" : "SUCCESS"));
		map.put("eventId", event.getType().toString());
		if (event.getClientId() != null)
            map.put("clientId", event.getClientId());
		return map;
	}

	public String getEventType() {
		return this.eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getRealmId() {
		return this.realmId;
	}

	public void setRealmId(String realmId) {
		this.realmId = realmId;
	}

	public String getErrorStatus() {
		return this.errorStatus;
	}

	public void setErrorStatus(String errorStatus) {
		this.errorStatus = errorStatus;
	}

	public String getResourceType() {
		return this.resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getOperationType() {
		return this.operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

	public String getClientId() {
		return this.clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getEventId() {
		return this.eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

}
