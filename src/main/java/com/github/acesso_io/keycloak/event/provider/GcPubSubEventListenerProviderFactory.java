package com.github.acesso_io.keycloak.event.provider;

import org.keycloak.Config.Scope;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class GcPubSubEventListenerProviderFactory implements EventListenerProviderFactory {

	private GcPubSubConfig cfg;

	@Override
	public EventListenerProvider create(KeycloakSession session) {
		return new GcPubSubEventListenerProvider(cfg);
	}

	@Override
	public void init(Scope config) {
		cfg = GcPubSubConfig.createFromScope(config);
	}

	@Override
	public void postInit(KeycloakSessionFactory factory) {

	}

	@Override
	public void close() {

	}

	@Override
	public String getId() {
		return "keycloak-to-gcpubsub";
	}

}
