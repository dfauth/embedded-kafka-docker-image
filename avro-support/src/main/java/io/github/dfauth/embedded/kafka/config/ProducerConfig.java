package io.github.dfauth.embedded.kafka.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Slf4j
@Configuration
public class ProducerConfig {

    @Value("${bootstrap.servers:127.0.0.1:9092}")
    private String bootstrapServers;

    @Bean
    @Qualifier("producer.props")
    public Map<String, Object> producerProps() {
        return Map.of(
                "bootstrap.servers", bootstrapServers
        );
    }

    @Bean
    public <K,V> ProducerFactory<K, V> producerFactory(@Qualifier("producer.props") Map<String, Object> producerProps,
                                                       @Qualifier("key.serializer") Serializer<K> keySerializer,
                                                       @Qualifier("value.serializer") Serializer<V> valueSerializer) {
        return () -> new KafkaProducer<>((Map<String, Object>) producerProps.get("producerProps"),keySerializer, valueSerializer);
    }

    @Bean
    public <K,V> KafkaTemplate<K, V> kafkaTemplate(ProducerFactory<K, V> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
