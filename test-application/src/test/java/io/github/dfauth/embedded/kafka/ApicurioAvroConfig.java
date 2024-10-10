package io.github.dfauth.embedded.kafka;

import io.apicurio.registry.serde.avro.AvroKafkaDeserializer;
import io.apicurio.registry.serde.avro.AvroKafkaSerializer;
import io.github.dfauth.embedded.kafka.test.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Slf4j
@Configuration()
public class ApicurioAvroConfig {

    @Value("${apicurio.registry.url}")
    private String url;

    @Value("${apicurio.registry.auto-register:true}")
    private Boolean autoRegister;

    @Bean
    @Qualifier("key.serializer")
    public Serializer<String> keySerializer() {
        return new StringSerializer();
    }

    @Bean
    @Qualifier("value.deserializer")
    public Supplier<Deserializer<User>> valueDeserializer(AvroKafkaDeserializer<User> deserializer) {
        return () -> deserializer;
    }

    @Bean
    @Qualifier("key.deserializer")
    public Supplier<Deserializer<String>> keyDeserializer() {
        return StringDeserializer::new;
    }

    @Bean
    @Qualifier("serde.config")
    public Map<String, Object> serdeConfig() {
        return Map.of("apicurio.registry.url",url, "apicurio.registry.auto-register",autoRegister);
    }

    @Bean
    @Qualifier("value.serializer")
    public AvroKafkaSerializer avroSerializer(@Qualifier("serde.config") Map<String, Object> config) {
        AvroKafkaSerializer serializer = new AvroKafkaSerializer();
        serializer.configure((Map<String, ?>) config.get("serdeConfig"), false);
        return serializer;
    }

    @Bean
    public AvroKafkaDeserializer avroDeserializer(@Qualifier("serde.config") Map<String, Object> config) {
        AvroKafkaDeserializer deserializer = new AvroKafkaDeserializer();
        deserializer.configure((Map<String, ?>) config.get("serdeConfig"), false);
        return deserializer;
    }

    @Bean
    public Receiver<Integer,io.github.dfauth.embedded.kafka.test.User> receiverBean() {
        return new Receiver<>();
    }

    @Bean
    public <K,V> CompletableFuture<V> receiver(Receiver<K,V> receiver) {
        return receiver.getF();
    }
}
