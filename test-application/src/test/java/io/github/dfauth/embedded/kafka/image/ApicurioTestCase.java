package io.github.dfauth.embedded.kafka.image;

import io.apicurio.registry.serde.avro.AvroKafkaSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.github.dfauth.embedded.kafka.image.FavouriteColour.RED;

@Slf4j
//@ActiveProfiles("local")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CommonConfig.class, ApicurioAvroConfig.class})
@TestPropertySource("classpath:application.yml")
public class ApicurioTestCase {

    @Value("${apicurio.registry.url}")
    private String url;

    @Autowired
    private AvroKafkaSerializer avroSerializer;

    @Autowired
    private CompletableFuture<String> f;

    private Map<String, Object> producerProps = Map.of(
            "bootstrap.servers", "127.0.0.1:9092"
    );

    private ProducerFactory<String, SpecificRecordBase> producerFactory = () -> new KafkaProducer<>(producerProps,
            new StringSerializer(),
            avroSerializer);

    private KafkaTemplate<String, SpecificRecordBase> kafkaTemplate = new KafkaTemplate<>(producerFactory);
    private String topic = "test";

    @Test
//    @Disabled
    public void testIt() {
        try {
            io.github.dfauth.embedded.kafka.image.test.User user = io.github.dfauth.embedded.kafka.image.test.User.newBuilder()
                    .setId(0)
                    .setUserId("fred")
                    .setFavoriteColor(RED)
                    .build();
            kafkaTemplate.send(topic, user)
                    .handle((sendResult, throwable) -> Optional.ofNullable(throwable).map(t -> {
                        log.info(t.getMessage());
                        return t;
                    }).orElseGet(() -> {
                        log.info("sendResult: "+sendResult.toString());
                        return null;
                    }))
                            ;
            log.info("received message: {}",f.get(1000, TimeUnit.MILLISECONDS));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }



}
