package io.github.dfauth.embedded.kafka.image;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
//@ActiveProfiles("local")
@SpringBootTest(classes = {CommonConfig.class, TestConfig.class})
public class TestCase {

    private String topic = "test";

    @Autowired
    private CompletableFuture<String> f;

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

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
