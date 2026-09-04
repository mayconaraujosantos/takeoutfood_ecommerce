package com.ifoodclone.order.config;

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

import com.ifoodclone.order.event.DeliveryStatusChangedEvent;

// The default consumer factory (application.yml's spring.kafka.consumer.*) is fixed to
// default-deserialize into PaymentProcessedEvent for the pre-existing "payment-events"
// listener. "delivery-events" carries a different shape, so it needs its own factory --
// same trick notification-service uses to consume two differently-shaped topics.
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.consumer.bootstrap-servers:${KAFKA_BOOTSTRAP_SERVERS:kafka:9092}}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, DeliveryStatusChangedEvent> deliveryConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-service");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.ifoodclone.order.event");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, DeliveryStatusChangedEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DeliveryStatusChangedEvent> deliveryKafkaListenerContainerFactory(
            ConsumerFactory<String, DeliveryStatusChangedEvent> deliveryConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, DeliveryStatusChangedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(deliveryConsumerFactory);
        return factory;
    }
}
