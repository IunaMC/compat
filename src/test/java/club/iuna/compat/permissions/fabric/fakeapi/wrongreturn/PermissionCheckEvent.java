package club.iuna.compat.permissions.fabric.fakeapi.wrongreturn;

import club.iuna.compat.permissions.fabric.fakeapi.Event;
import club.iuna.compat.permissions.fabric.fakeapi.FakeSource;
import club.iuna.compat.permissions.fabric.fakeapi.TriState;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PermissionCheckEvent {
    Event<PermissionCheckEvent> EVENT = Event.create(PermissionCheckEvent.class, callbacks -> (source, permission) -> {
        for (PermissionCheckEvent callback : callbacks) {
            if (callback.onPermissionCheck(source, permission)) {
                return true;
            }
        }
        return false;
    });

    Boolean onPermissionCheck(FakeSource source, String permission);
}
