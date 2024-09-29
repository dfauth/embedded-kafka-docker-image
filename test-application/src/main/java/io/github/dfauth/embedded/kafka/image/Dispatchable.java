package io.github.dfauth.embedded.kafka.image;

import io.github.dfauth.embedded.kafka.image.dispatchable.CreateEvent;
import io.github.dfauth.embedded.kafka.image.dispatchable.DeleteEvent;
import io.github.dfauth.embedded.kafka.image.dispatchable.UpdateEvent;

public interface Dispatchable {

    void dispatch(DispatchableHandler handler);

    interface DispatchableHandler {
        void handle(CreateEvent e);
        void handle(UpdateEvent e);
        void handle(DeleteEvent e);
    }
}
