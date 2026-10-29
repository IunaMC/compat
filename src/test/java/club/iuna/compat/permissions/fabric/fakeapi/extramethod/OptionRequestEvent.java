package club.iuna.compat.permissions.fabric.fakeapi.extramethod;

import club.iuna.compat.permissions.fabric.fakeapi.Event;
import club.iuna.compat.permissions.fabric.fakeapi.FakeSource;
import club.iuna.compat.permissions.fabric.fakeapi.TriState;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface OptionRequestEvent {
    Event<OptionRequestEvent> EVENT = Event.create(OptionRequestEvent.class, callbacks -> null);

    Optional<String> onOptionRequest(FakeSource source, String key);

    Optional<String> second(FakeSource source);
}
