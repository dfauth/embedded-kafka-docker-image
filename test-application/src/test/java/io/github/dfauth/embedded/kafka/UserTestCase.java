package io.github.dfauth.embedded.kafka;

import io.github.dfauth.embedded.kafka.dispatchable.DeleteEvent;
import io.github.dfauth.embedded.kafka.dispatchable.UpdateEvent;
import io.github.dfauth.embedded.kafka.test.FavouriteColour;
import io.github.dfauth.embedded.kafka.test.User;
import io.github.dfauth.embedded.kafka.util.RegistryClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecord;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;

import static io.github.dfauth.embedded.kafka.test.FavouriteColour.RED;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class UserTestCase implements Dispatchable.DispatchableHandler {

    private DeleteEvent deleteEvent = null;
    private UpdateEvent updateEvent = null;

    @Test
    public void testIt() {
        User user = User.newBuilder()
                .setUserId("fred")
                .setId(0)
                .setFavoriteColor(RED)
                .build();

        FavouriteColour colour = user.getFavoriteColor();
        assertEquals(RED, colour);

        Dispatchable dispatchable = io.github.dfauth.embedded.kafka.dispatchable.UpdateEvent.newBuilder().setPayload(user).build();
        dispatchable.dispatch(this);
        assertNotNull(updateEvent);
        assertNull(deleteEvent);
    }

    @Override
    public void handle(UpdateEvent e) {
        updateEvent = e;
    }

    @Override
    public void handle(DeleteEvent e) {
        deleteEvent = e;
    }

    @Test
    public void testRegistryClient() throws FileNotFoundException {
        RegistryClient.streamClasses(new FileInputStream("target/test-application-0.0.1-SNAPSHOT.jar"))
                .filter(SpecificRecord.class::isAssignableFrom)
                .map(clazz -> {
                    try {
                        Field schemaField = clazz.getField("SCHEMA$");
                        schemaField.setAccessible(true);
                        return (Schema) schemaField.get(clazz);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        log.error(e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                })
                .forEach(schema -> {
                    log.info("schema is: {}",schema.toString());
                });
    }
}
