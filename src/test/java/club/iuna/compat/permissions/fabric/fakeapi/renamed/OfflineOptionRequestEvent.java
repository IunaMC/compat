package club.iuna.compat.permissions.fabric.fakeapi.renamed;

import club.iuna.compat.permissions.fabric.fakeapi.Event;
import club.iuna.compat.permissions.fabric.fakeapi.FakeSource;
import club.iuna.compat.permissions.fabric.fakeapi.TriState;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface OfflineOptionRequestEvent {
    Event<OfflineOptionRequestEvent> EVENT = Event.create(OfflineOptionRequestEvent.class, callbacks -> (uuid, key) -> {
        CompletableFuture<Optional<String>> result = CompletableFuture.completedFuture(Optional.empty());
        for (OfflineOptionRequestEvent callback : callbacks) {
            result = result.thenCompose(value -> value.isPresent() ? CompletableFuture.completedFuture(value) : callback.onOptionRequest(uuid, key));
        }
        return result;
    });

    CompletableFuture<Optional<String>> onOptionRequest(UUID uuid, String key);
}
