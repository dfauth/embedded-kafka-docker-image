package io.github.dfauth.embedded.kafka.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Configuration
public class ConsumerConfig {

    @Value("${bootstrap.servers:127.0.0.1:9092}")
    private String bootstrapServers;

    @Value("${comsumer.group.id:group_id}")
    private String groupId;

    @Bean
    public <K,V> ConsumerFactory<K, V> consumerFactory(@Qualifier("key.deserializer") Supplier<Deserializer<K>> keyDeserializer, @Qualifier("value.deserializer") Supplier<Deserializer<V>> valueDeserializer) {

        // Creating a Map of string-object pairs
        Map<String, Object> config = new HashMap<>();

        // Adding the Configuration
        config.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, groupId);
        return new DefaultKafkaConsumerFactory<>(config, keyDeserializer, valueDeserializer);
    }

    // Creating a Listener
    @Bean
    public <K,V> KafkaListenerContainerFactory kafkaListenerContainerFactory(ConsumerFactory<? super K, ? super V> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<K, V> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
