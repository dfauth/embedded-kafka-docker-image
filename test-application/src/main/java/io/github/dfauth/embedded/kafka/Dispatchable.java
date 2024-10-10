package io.github.dfauth.embedded.kafka;

import io.github.dfauth.embedded.kafka.dispatchable.DeleteEvent;
import io.github.dfauth.embedded.kafka.dispatchable.UpdateEvent;

public interface Dispatchable {

    void dispatch(DispatchableHandler handler);

    interface DispatchableHandler {
        void handle(UpdateEvent e);
        void handle(DeleteEvent e);
    }
}
