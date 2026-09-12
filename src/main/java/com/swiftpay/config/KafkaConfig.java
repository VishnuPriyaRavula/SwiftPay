package com.swiftpay.config;

import com.swiftpay.messaging.PaymentEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.*;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {
    @Bean NewTopic paymentInitiatedTopic() { return TopicBuilder.name("payment-initiated").partitions(6).replicas(1).build(); }
    @Bean NewTopic paymentCompletedTopic() { return TopicBuilder.name("payment-completed").partitions(6).replicas(1).build(); }
    @Bean NewTopic paymentFailedTopic() { return TopicBuilder.name("payment-failed").partitions(6).replicas(1).build(); }
    @Bean ProducerFactory<String, PaymentEvent> producerFactory(org.springframework.boot.autoconfigure.kafka.KafkaProperties properties) {
        var config = properties.buildProducerProperties(null);
        config.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }
    @Bean KafkaTemplate<String, PaymentEvent> kafkaTemplate(ProducerFactory<String, PaymentEvent> factory) { return new KafkaTemplate<>(factory); }
    @Bean DefaultErrorHandler kafkaErrorHandler() {
        return new DefaultErrorHandler(new FixedBackOff(1000L, 3L));
    }
}
