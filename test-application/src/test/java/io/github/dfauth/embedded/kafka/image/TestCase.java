package io.github.dfauth.embedded.kafka.image;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
//@ActiveProfiles("local")
@SpringBootTest(classes = {TestConfig.class})
public class TestCase {

    @Autowired
    private CompletableFuture<String> f;

    private Map<String, Object> producerProps = Map.of(
            "bootstrap.servers", "127.0.0.1:9092"
    );
    private ProducerFactory<String, String> producerFactory = () -> new KafkaProducer<>(producerProps, new StringSerializer(), new StringSerializer());
    private KafkaTemplate<String,String> kafkaTemplate = new KafkaTemplate<>(producerFactory);
    private String topic = "test";

    @Test
    @Disabled
    public void testIt() throws ExecutionException, InterruptedException, TimeoutException {
        kafkaTemplate.send(topic, "TEST")
                .handle((sendResult, throwable) -> Optional.ofNullable(throwable).map(t -> {
                    log.info(t.getMessage());
                    return t;
                }).orElseGet(() -> {
                    log.info("sendResult: "+sendResult.toString());
                    return null;
                }))
                        ;
        log.info("received message: {}",f.get(1000, TimeUnit.MILLISECONDS));
    }


}
