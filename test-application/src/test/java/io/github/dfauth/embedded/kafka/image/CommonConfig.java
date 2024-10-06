package io.github.dfauth.embedded.kafka.image;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Slf4j
@EnableKafka
@Configuration
public class CommonConfig {

    @Bean
    @Qualifier("producer.props")
    public Map<String, Object> producerProps() {
        return Map.of(
                "bootstrap.servers", "127.0.0.1:9092"
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


    @Bean
    public <K,V> ConsumerFactory<K, V> consumerFactory(@Qualifier("key.deserializer") Supplier<Deserializer<K>> serializer, @Qualifier("value.deserializer") Supplier<Deserializer<V>> deserializer) {

        // Creating a Map of string-object pairs
        Map<String, Object> config = new HashMap<>();

        // Adding the Configuration
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "group_id");
//        config.put( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        config.put( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(config, serializer, deserializer);
    }

    // Creating a Listener
    @Bean
    public <K,V> KafkaListenerContainerFactory kafkaListenerContainerFactory(ConsumerFactory<? super K, ? super V> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<K, V> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public <K,V> CompletableFuture<V> receiver(Receiver<K,V> receiver) {
        return receiver.getF();
    }

}
