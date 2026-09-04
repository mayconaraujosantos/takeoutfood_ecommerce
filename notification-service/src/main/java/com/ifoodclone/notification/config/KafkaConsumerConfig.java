package com.ifoodclone.notification.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.ifoodclone.notification.event.DeliveryStatusChangedEvent;
import com.ifoodclone.notification.event.OrderStatusChangedEvent;

// order-events and delivery-events carry two different JSON shapes, and each producer
// (order-service, delivery-service) tags its own messages with its own event class's
// FQCN, which doesn't exist in this service. A single default.type property (as used by
// order-service for its one inbound event type) can't cover two shapes, so each topic
// gets its own ConsumerFactory/ContainerFactory pinned to its own local event class.
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.consumer.bootstrap-servers:${KAFKA_BOOTSTRAP_SERVERS:kafka:9092}}")
    private String bootstrapServers;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderStatusChangedEvent> orderKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderStatusChangedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(OrderStatusChangedEvent.class));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DeliveryStatusChangedEvent> deliveryKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DeliveryStatusChangedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(DeliveryStatusChangedEvent.class));
        return factory;
    }

    private <T> ConsumerFactory<String, T> consumerFactory(Class<T> targetType) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-service");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, targetType.getName());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.ifoodclone.notification.event");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
                new JsonDeserializer<>(targetType, false));
    }
}
