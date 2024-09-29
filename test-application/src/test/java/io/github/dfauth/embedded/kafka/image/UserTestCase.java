package io.github.dfauth.embedded.kafka.image;

import io.github.dfauth.embedded.kafka.image.test.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static io.github.dfauth.embedded.kafka.image.FavouriteColour.RED;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class UserTestCase {

    @Test
    public void testIt() {
        User user = User.newBuilder()
                .setUserId("fred")
                .setId(0)
                .setFavoriteColor(RED)
                .build();

        FavouriteColour colour = user.getFavoriteColor();
        assertEquals(RED, colour);
    }
}
