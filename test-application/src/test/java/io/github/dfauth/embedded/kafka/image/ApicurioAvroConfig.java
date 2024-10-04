package io.github.dfauth.embedded.kafka.image;

import io.apicurio.registry.serde.avro.AvroKafkaDeserializer;
import io.apicurio.registry.serde.avro.AvroKafkaSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Slf4j
@Configuration()
public class ApicurioAvroConfig {

    @Value("${apicurio.registry.url}")
    private String url;

    @Bean
    @Qualifier("serde.config")
    public Map<String, Object> serdeConfig() {
        return Map.of("apicurio.registry.url",url);
    }

    @Bean
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
    public Receiver<io.github.dfauth.embedded.kafka.image.test.User> receiverBean() {
        return new Receiver<>();
    }

}
