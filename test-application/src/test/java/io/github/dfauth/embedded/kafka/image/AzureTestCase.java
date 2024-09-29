package io.github.dfauth.embedded.kafka.image;

import com.azure.core.models.MessageContent;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.apacheavro.SchemaRegistryApacheAvroSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.Serializer;
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
@SpringBootTest(classes = {CommonConfig.class, AzureAvroConfig.class})
public class AzureTestCase {

    @Autowired
    private SchemaRegistryApacheAvroSerializer avroSerializer;

    @Autowired
    private CompletableFuture<String> f;

    private Map<String, Object> producerProps = Map.of(
            "bootstrap.servers", "127.0.0.1:9092"
    );

    private ProducerFactory<String, SpecificRecordBase> producerFactory = () -> new KafkaProducer<>(producerProps,
            new StringSerializer(),
            (Serializer<SpecificRecordBase>) (s, specificRecordBase) -> {
                TypeReference<? extends MessageContent> tr = TypeReference.createInstance(MessageContent.class);
                return avroSerializer.serialize(specificRecordBase, tr).getBodyAsBinaryData().toBytes();
        });

    private KafkaTemplate<String, SpecificRecordBase> kafkaTemplate = new KafkaTemplate<>(producerFactory);
    private String topic = "test";

    @Test
    @Disabled
    public void testIt() throws ExecutionException, InterruptedException, TimeoutException {
        io.github.dfauth.embedded.kafka.image.test.User user = io.github.dfauth.embedded.kafka.image.test.User.newBuilder()
                .setId(0)
                        .setUserId("fred").build();
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
    }



}
