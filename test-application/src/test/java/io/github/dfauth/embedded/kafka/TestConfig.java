package io.github.dfauth.embedded.kafka;

import io.github.dfauth.embedded.kafka.Receiver;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Slf4j
@EnableKafka
@Configuration
public class TestConfig {

    @Bean
    @Qualifier("value.serializer")
    public Serializer<String> valueSerializer() {
        return new StringSerializer();
    }

    @Bean
    @Qualifier("key.serializer")
    public Serializer<String> keySerializer() {
        return new StringSerializer();
    }

    @Bean
    @Qualifier("value.deserializer")
    public Supplier<Deserializer<String>> valueDeserializer() {
        return StringDeserializer::new;
    }

    @Bean
    @Qualifier("key.deserializer")
    public Supplier<Deserializer<String>> keyDeserializer() {
        return StringDeserializer::new;
    }

    @Bean
    public Receiver<String,String> receiverBean() {
        return new Receiver<>();
    }

    @Bean
    public <K,V> CompletableFuture<V> receiver(Receiver<K,V> receiver) {
        return receiver.getF();
    }
}
