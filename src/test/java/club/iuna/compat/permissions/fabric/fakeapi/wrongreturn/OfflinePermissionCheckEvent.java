package club.iuna.compat.permissions.fabric.fakeapi.wrongreturn;

import club.iuna.compat.permissions.fabric.fakeapi.Event;
import club.iuna.compat.permissions.fabric.fakeapi.FakeSource;
import club.iuna.compat.permissions.fabric.fakeapi.TriState;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface OfflinePermissionCheckEvent {
    Event<OfflinePermissionCheckEvent> EVENT = Event.create(OfflinePermissionCheckEvent.class, callbacks -> (uuid, permission) -> {
        CompletableFuture<TriState> result = CompletableFuture.completedFuture(TriState.DEFAULT);
        for (OfflinePermissionCheckEvent callback : callbacks) {
            result = result.thenCompose(state -> state != TriState.DEFAULT ? CompletableFuture.completedFuture(state) : callback.onPermissionCheck(uuid, permission));
        }
        return result;
    });

    CompletableFuture<TriState> onPermissionCheck(UUID uuid, String permission);
}
