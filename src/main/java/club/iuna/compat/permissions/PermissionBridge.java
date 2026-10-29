package club.iuna.compat.permissions;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

/**
 * The decision logic between the Fabric permission events and the backend. It knows nothing about
 * Fabric types or LuckPerms types, so it can be tested on its own.
 *
 * <p>The contract mirrors LuckPerms' own Fabric listener: an online player is answered from the
 * backend, the console and other sources get {@link Verdict#DEFAULT}, and an offline UUID is
 * answered from a stored user. Nothing here ever throws into the caller: a failure is logged,
 * counted and answered with "no opinion", which leaves the mod's own default in force.
 */
public final class PermissionBridge {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final PermissionBackend backend;
    private final SubjectResolver resolver;
    private final BridgeStats stats;
    private final BooleanSupplier answerOffline;
    private final BooleanSupplier answerOptions;
    private final BooleanSupplier logChecks;

    public PermissionBridge(PermissionBackend backend, SubjectResolver resolver, BridgeStats stats,
                            BooleanSupplier answerOffline, BooleanSupplier answerOptions, BooleanSupplier logChecks) {
        this.backend = backend;
        this.resolver = resolver;
        this.stats = stats;
        this.answerOffline = answerOffline;
        this.answerOptions = answerOptions;
        this.logChecks = logChecks;
    }

    public BridgeStats stats() {
        return stats;
    }

    public Verdict check(Object source, String node) {
        Verdict verdict = Verdict.DEFAULT;
        try {
            Optional<Subject> subject = resolver.resolve(source);
            if (subject.isPresent()) {
                verdict = backend.check(subject.get(), node);
            }
            if (logChecks.getAsBoolean()) {
                LOGGER.info("[permissions] {} {} -> {}", subject.map(s -> s.id().toString()).orElse("<non-player>"), node, verdict);
            }
        } catch (Throwable error) {
            fail(error);
        }
        stats.record(verdict);
        return verdict;
    }

    public Optional<String> option(Object source, String key) {
        if (!answerOptions.getAsBoolean()) {
            return Optional.empty();
        }
        Optional<String> value = Optional.empty();
        try {
            Optional<Subject> subject = resolver.resolve(source);
            if (subject.isPresent()) {
                value = backend.option(subject.get(), key);
            }
            if (logChecks.getAsBoolean()) {
                LOGGER.info("[options] {} {} -> {}", subject.map(s -> s.id().toString()).orElse("<non-player>"), key, value.orElse("<none>"));
            }
        } catch (Throwable error) {
            fail(error);
        }
        stats.recordOption(value.isPresent());
        return value;
    }

    public CompletableFuture<Verdict> checkOffline(UUID id, String node) {
        if (!answerOffline.getAsBoolean()) {
            return CompletableFuture.completedFuture(Verdict.DEFAULT);
        }
        CompletableFuture<Verdict> future;
        try {
            future = backend.checkOffline(id, node);
        } catch (Throwable error) {
            future = CompletableFuture.failedFuture(error);
        }
        return future.handle((verdict, error) -> {
            Verdict result = Verdict.DEFAULT;
            if (error != null) {
                fail(error);
            } else if (verdict != null) {
                result = verdict;
            }
            if (logChecks.getAsBoolean()) {
                LOGGER.info("[permissions] {} {} -> {} (offline)", id, node, result);
            }
            stats.record(result);
            return result;
        });
    }

    public CompletableFuture<Optional<String>> optionOffline(UUID id, String key) {
        if (!answerOffline.getAsBoolean() || !answerOptions.getAsBoolean()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        CompletableFuture<Optional<String>> future;
        try {
            future = backend.optionOffline(id, key);
        } catch (Throwable error) {
            future = CompletableFuture.failedFuture(error);
        }
        return future.handle((value, error) -> {
            Optional<String> result = Optional.empty();
            if (error != null) {
                fail(error);
            } else if (value != null) {
                result = value;
            }
            stats.recordOption(result.isPresent());
            return result;
        });
    }

    private void fail(Throwable error) {
        Throwable cause = error instanceof java.util.concurrent.CompletionException && error.getCause() != null
                ? error.getCause() : error;
        // Log the first failure with its stack trace and the following ones in one line, to avoid
        // flooding the log if the backend breaks while thousands of checks are running.
        if (stats.errors() == 0) {
            LOGGER.error("The permission backend failed; answering \"no opinion\" so the mods keep their defaults", cause);
        } else {
            LOGGER.debug("The permission backend failed again: {}", cause.toString());
        }
        stats.recordError(cause);
    }
}
