package io.github.dfauth.embedded.kafka;

import io.github.dfauth.embedded.kafka.test.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.github.dfauth.embedded.kafka.test.FavouriteColour.RED;
import static io.github.dfauth.embedded.kafka.util.CompletableFutureUtil.onException;

@Slf4j
//@ActiveProfiles("local")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {io.github.dfauth.embedded.kafka.config.ConsumerConfig.class, io.github.dfauth.embedded.kafka.config.ProducerConfig.class, ApicurioAvroConfig.class})
@TestPropertySource("classpath:application.yml")
public class ApicurioTestCase {

    @Value("${apicurio.registry.url}")
    private String url;

    private String topic = "test";

    @Autowired
    private CompletableFuture<User> f;

    @Autowired
    private KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;

    @Test
    @Disabled
    public void testIt() {
        try {
            io.github.dfauth.embedded.kafka.test.User user = io.github.dfauth.embedded.kafka.test.User.newBuilder()
                    .setId(0)
                    .setUserId("fred")
                    .setFavoriteColor(RED)
                    .build();
            onException(kafkaTemplate.send(topic, user), e -> log.error(e.getMessage(), e));
                            ;
            User u = f.get(1000, TimeUnit.MILLISECONDS);
            log.info("received message: {}",u);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }



}
