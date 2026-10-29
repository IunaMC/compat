package club.iuna.compat.permissions;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/** In-memory backend: answers from maps, can be told to fail. */
class FakeBackend implements PermissionBackend {
    final Map<String, Verdict> online = new HashMap<>();
    final Map<String, Verdict> offline = new HashMap<>();
    final Map<String, String> options = new HashMap<>();
    final List<String> registered = new CopyOnWriteArrayList<>();
    RuntimeException failure;

    @Override
    public Verdict check(Subject subject, String node) {
        fail();
        return online.getOrDefault(node, Verdict.DEFAULT);
    }

    @Override
    public Optional<String> option(Subject subject, String key) {
        fail();
        return Optional.ofNullable(options.get(key));
    }

    @Override
    public CompletableFuture<Verdict> checkOffline(UUID id, String node) {
        if (failure != null) {
            return CompletableFuture.failedFuture(failure);
        }
        return CompletableFuture.completedFuture(offline.getOrDefault(node, Verdict.DEFAULT));
    }

    @Override
    public CompletableFuture<Optional<String>> optionOffline(UUID id, String key) {
        if (failure != null) {
            return CompletableFuture.failedFuture(failure);
        }
        return CompletableFuture.completedFuture(Optional.ofNullable(options.get(key)));
    }

    @Override
    public void registerNodes(Collection<String> nodes) {
        registered.addAll(nodes);
    }

    private void fail() {
        if (failure != null) {
            throw failure;
        }
    }
}
