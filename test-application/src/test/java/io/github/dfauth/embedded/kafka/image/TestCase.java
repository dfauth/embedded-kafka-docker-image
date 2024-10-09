package io.github.dfauth.embedded.kafka.image;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.github.dfauth.embedded.kafka.util.CompletableFutureUtil.onException;

@Slf4j
//@ActiveProfiles("local")
@SpringBootTest(classes = {io.github.dfauth.embedded.kafka.config.ConsumerConfig.class, io.github.dfauth.embedded.kafka.config.ProducerConfig.class, TestConfig.class})
public class TestCase {

    private String topic = "test";

    @Autowired
    private CompletableFuture<String> f;

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Test
    @Disabled
    public void testIt() throws ExecutionException, InterruptedException, TimeoutException {
        onException(kafkaTemplate.send(topic, "TEST"), e -> log.error(e.getMessage(), e));
                        ;
        log.info("received message: {}",f.get(1000, TimeUnit.MILLISECONDS));
    }


}
