package io.github.dfauth.kafka.cache;

import io.github.dfauth.kafka.cache.config.KafkaCacheConfig;
import io.github.dfauth.kafka.cache.config.TestConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.Duration;

import static io.github.dfauth.embedded.kafka.util.CompletableFutureUtil.toMono;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@EmbeddedKafka(ports = 9092, topics = "topic")
@SpringBootTest(classes = {
        io.github.dfauth.embedded.kafka.config.ConsumerConfig.class,
        io.github.dfauth.embedded.kafka.config.ProducerConfig.class,
        KafkaCacheConfig.class,
        TestConfig.class})
public class TestCase {

    private static final String TOPIC = "topic";
    @Autowired
    public KafkaCache<String,String> cache;

    @Autowired
    public KafkaTemplate<String,String> template;

    @Test
    public void testIt() {
        assertNotNull(cache);
        SendResult<String, String> sendResult = toMono(template.send(TOPIC, "key", "value")).block(Duration.ofSeconds(1));
        log.info("metadata: {}", sendResult.getRecordMetadata());
        assertNotNull(sendResult);
    }

}
