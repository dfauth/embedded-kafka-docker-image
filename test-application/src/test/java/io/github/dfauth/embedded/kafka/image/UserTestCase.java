package io.github.dfauth.embedded.kafka.image;

import io.github.dfauth.embedded.kafka.FavouriteColour;
import io.github.dfauth.embedded.kafka.image.dispatchable.CreateEvent;
import io.github.dfauth.embedded.kafka.image.dispatchable.DeleteEvent;
import io.github.dfauth.embedded.kafka.image.dispatchable.UpdateEvent;
import io.github.dfauth.embedded.kafka.image.test.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static io.github.dfauth.embedded.kafka.FavouriteColour.RED;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class UserTestCase implements Dispatchable.DispatchableHandler {

    private CreateEvent createEvent = null;
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

        Dispatchable dispatchable = io.github.dfauth.embedded.kafka.image.dispatchable.CreateEvent.newBuilder().setUser(user).build();
        dispatchable.dispatch(this);
        assertNotNull(createEvent);
        assertNull(updateEvent);
        assertNull(deleteEvent);
    }

    @Override
    public void handle(CreateEvent e) {
        createEvent = e;
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
