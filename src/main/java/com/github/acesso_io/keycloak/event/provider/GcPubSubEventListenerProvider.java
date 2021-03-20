package com.github.acesso_io.keycloak.event.provider;


import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GcPubSubEventListenerProvider implements EventListenerProvider {

	private GcPubSubConfig cfg;
	//private ConnectionFactory factory;

	public GcPubSubEventListenerProvider(GcPubSubConfig cfg) {
		this.cfg = cfg;

	}

	@Override
	public void close() {

	}

	@Override
	public void onEvent(Event event) {
		EventClientNotificationGcpsMsg msg = EventClientNotificationGcpsMsg.create(event);
		Map<String, String> messageAttributes = GcPubSubAttributes.createMap(event);
		String messageString = GcPubSubConfig.writeAsJson(msg, true);
		String topicId = cfg.getAdminEventTopicId();

		this.publishNotification(topicId, messageString, messageAttributes);
	}

	@Override
	public void onEvent(AdminEvent event, boolean includeRepresentation) {
		EventAdminNotificationGcpsMsg msg = EventAdminNotificationGcpsMsg.create(event);
		Map<String, String> messageAttributes = GcPubSubAttributes.createMap(event);
		String messageString = GcPubSubConfig.writeAsJson(msg, true);
		String topicId = cfg.getAdminEventTopicId();

		this.publishNotification(topicId, messageString, messageAttributes);
	}	

	private void publishNotification(String topicId, String messageString, Map<String, String> attributes) 
	{

		TopicName topicName = TopicName.of(cfg.getProjectId(), topicId);

		Publisher publisher = null;

		try {
		// Create a publisher instance with default settings bound to the topic
		publisher = Publisher.newBuilder(topicName).build();

		String message = messageString;
		ByteString data = ByteString.copyFromUtf8(message);
		PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).putAllAttributes(attributes).build();

		// Once published, returns a server-assigned message id (unique within the topic)
		ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
		String messageId = messageIdFuture.get();
		System.out.println("Published message ID: " + messageId);
		} 
		catch (IOException|ExecutionException|InterruptedException ex) {
			System.err.println("keycloak-to-gcpubsub ERROR sending message: " + attributes);
			ex.printStackTrace();
		}
		finally {
			if (publisher != null) {
				try {
					// When finished with the publisher, shutdown to free up resources.
				publisher.shutdown();
				publisher.awaitTermination(1, TimeUnit.MINUTES);
				} catch (InterruptedException ex) {
					System.err.println("keycloak-to-gcpubsub ERROR sending message: " + attributes);
					ex.printStackTrace();
				}
			}
		}

		// try {
		// 	Connection conn = factory.newConnection();
		// 	Channel channel = conn.createChannel();
			
		// 	channel.basicPublish(cfg.getExchange(), routingKey, props, messageString.getBytes());
		// 	System.out.println("keycloak-to-rabbitmq SUCCESS sending message: " + routingKey);
		// 	channel.close();
		// 	conn.close();

		// } catch (Exception ex) {
		// 	System.err.println("keycloak-to-rabbitmq ERROR sending message: " + routingKey);
		// 	ex.printStackTrace();
		// }
	}

}
