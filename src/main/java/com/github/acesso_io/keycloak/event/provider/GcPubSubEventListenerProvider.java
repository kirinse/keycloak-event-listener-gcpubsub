package com.github.acesso_io.keycloak.event.provider;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerTransaction;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

import org.jboss.logging.Logger;
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

    private static final Logger logger = Logger.getLogger(GcPubSubEventListenerProvider.class);
    private final GcPubSubConfig cfg;
    private final EventListenerTransaction tx = new EventListenerTransaction(this::publishAdminEvent, this::publishEvent);

    public GcPubSubEventListenerProvider(GcPubSubConfig cfg, KeycloakSession session) {
        this.cfg = cfg;
        session.getTransactionManager().enlistAfterCompletion(tx);
    }

    @Override
    public void close() {
    }

    @Override
    public void onEvent(Event event) {
        tx.addEvent(event);
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        tx.addAdminEvent(event, includeRepresentation);
    }

    private void publishEvent(Event event) {
        EventClientNotificationGcpsMsg msg = EventClientNotificationGcpsMsg.create(event);
        Map<String, String> messageAttributes = GcPubSubAttributes.createMap(event);
        String messageString = GcPubSubConfig.writeAsJson(msg, true);
        String topicId = cfg.getAdminEventTopicId();

        this.publishNotification(topicId, messageString, messageAttributes);
    }

    private void publishAdminEvent(AdminEvent event, boolean includeRepresentation) {
        EventAdminNotificationGcpsMsg msg = EventAdminNotificationGcpsMsg.create(event);
        Map<String, String> messageAttributes = GcPubSubAttributes.createMap(event);
        String messageString = GcPubSubConfig.writeAsJson(msg, true);
        String topicId = cfg.getAdminEventTopicId();

        this.publishNotification(topicId, messageString, messageAttributes);
    }

    private void publishNotification(String topicId, String messageString, Map<String, String> attributes) {
        TopicName topicName = TopicName.of(cfg.getProjectId(), topicId);
        Publisher publisher = null;

        try {
            // Create a publisher instance with default settings bound to the topic
            publisher = getPublisher(topicName);
            ByteString data = ByteString.copyFromUtf8(messageString);
            PubsubMessage pubsubMessage = PubsubMessage
                    .newBuilder()
                    .setData(data)
                    .putAllAttributes(attributes)
                    .build();

            // Once published, returns a server-assigned message id (unique within the topic)
            ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
            String messageId = messageIdFuture.get();
            logger.info("Published message ID: " + messageId);
        } catch (IOException | ExecutionException | InterruptedException ex) {
            logger.error("keycloak-to-gcpubsub ERROR sending message: " + attributes, ex);
        } finally {
            if (publisher != null) {
                try {
                    // When finished with the publisher, shutdown to free up resources.
                    publisher.shutdown();
                    publisher.awaitTermination(1, TimeUnit.MINUTES);
                } catch (InterruptedException ex) {
                    logger.error("keycloak-to-gcpubsub ERROR shutting down publisher: " + attributes, ex);
                }
            }
        }

    }

    private Publisher getPublisher(TopicName topicName) throws IOException {
        String hostport = System.getenv("PUBSUB_EMULATOR_HOST");
        if (hostport != null) {
            ManagedChannel channel = ManagedChannelBuilder.forTarget(hostport).usePlaintext().build();
            TransportChannelProvider channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
            CredentialsProvider credentialsProvider = NoCredentialsProvider.create();
            return Publisher.newBuilder(topicName)
                    .setChannelProvider(channelProvider)
                    .setCredentialsProvider(credentialsProvider)
                    .build();
        }
        return Publisher
                .newBuilder(topicName)
                .build();
    }
}
