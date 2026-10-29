package club.iuna.compat.permissions.fabric.fakeapi.partial;

import club.iuna.compat.permissions.fabric.fakeapi.Event;
import club.iuna.compat.permissions.fabric.fakeapi.FakeSource;
import club.iuna.compat.permissions.fabric.fakeapi.TriState;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface OptionRequestEvent {
    Event<OptionRequestEvent> EVENT = Event.create(OptionRequestEvent.class, callbacks -> (source, key) -> {
        for (OptionRequestEvent callback : callbacks) {
            Optional<String> value = callback.onOptionRequest(source, key);
            if (value.isPresent()) {
                return value;
            }
        }
        return Optional.empty();
    });

    Optional<String> onOptionRequest(FakeSource source, String key);
}
