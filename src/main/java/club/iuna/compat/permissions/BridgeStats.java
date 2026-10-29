package club.iuna.compat.permissions;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

/**
 * Counters shown by {@code /iunacompat status}. {@link LongAdder} keeps the hot path cheap when
 * several threads (server, login, async commands) ask at once.
 */
public final class BridgeStats {
    private final LongAdder allowed = new LongAdder();
    private final LongAdder denied = new LongAdder();
    private final LongAdder undecided = new LongAdder();
    private final LongAdder optionsAnswered = new LongAdder();
    private final LongAdder optionsMissed = new LongAdder();
    private final LongAdder errors = new LongAdder();
    private final AtomicReference<String> lastError = new AtomicReference<>();

    void record(Verdict verdict) {
        switch (verdict) {
            case TRUE -> allowed.increment();
            case FALSE -> denied.increment();
            case DEFAULT -> undecided.increment();
        }
    }

    void recordOption(boolean answered) {
        (answered ? optionsAnswered : optionsMissed).increment();
    }

    void recordError(Throwable error) {
        errors.increment();
        lastError.set(error.getClass().getSimpleName() + ": " + error.getMessage());
    }

    public long allowed() {
        return allowed.sum();
    }

    public long denied() {
        return denied.sum();
    }

    public long undecided() {
        return undecided.sum();
    }

    public long optionsAnswered() {
        return optionsAnswered.sum();
    }

    public long optionsMissed() {
        return optionsMissed.sum();
    }

    public long errors() {
        return errors.sum();
    }

    public String lastError() {
        return lastError.get();
    }
}
