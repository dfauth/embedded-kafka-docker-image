package io.github.dfauth.apicurio.serde;

import io.apicurio.registry.serde.SerdeConfig;
import io.apicurio.registry.serde.avro.AvroSerde;
import io.github.dfauth.embedded.kafka.test.FavouriteColour;
import io.github.dfauth.embedded.kafka.test.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

@Slf4j
public class UnitTest {

    private static final String TOPIC = User.class.getCanonicalName();
    private static final User TEST = io.github.dfauth.embedded.kafka.test.User.newBuilder().setId(1).setUserId("fred").setFavoriteColor(FavouriteColour.RED).build();
    private Map<String, ?> config = Map.of(
            SerdeConfig.REGISTRY_URL, "http://localhost:8080/apis/registry/v2",
            "apicurio.registry.use-specific-avro-reader", true,
            "apicurio.registry.auto-register", true
    );

    @Test
    public void testIt() {
        try {
            var serde = new AvroSerde<User>();
            serde.configure(config, false);
            try(serde) {
                var serializer = serde.serializer();
                var deserializer = serde.deserializer();

                var bytes = serializer.serialize(TOPIC, TEST);
                assertNotNull(bytes);
                assertTrue(bytes.length > 0);
                var user = deserializer.deserialize(TOPIC, bytes);
                assertNotNull(user);
                assertEquals(TEST, user);

                bytes = serializer.serialize(TOPIC, TEST);
                assertNotNull(bytes);
                assertTrue(bytes.length > 0);
                user = deserializer.deserialize(TOPIC, bytes);
                assertNotNull(user);
                assertEquals(TEST, user);

            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
