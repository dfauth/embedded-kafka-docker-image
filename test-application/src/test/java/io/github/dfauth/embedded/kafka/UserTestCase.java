package io.github.dfauth.embedded.kafka;

import io.github.dfauth.embedded.kafka.dispatchable.DeleteEvent;
import io.github.dfauth.embedded.kafka.dispatchable.UpdateEvent;
import io.github.dfauth.embedded.kafka.test.FavouriteColour;
import io.github.dfauth.embedded.kafka.test.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

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
}
