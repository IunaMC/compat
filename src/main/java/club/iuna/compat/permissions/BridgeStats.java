package club.iuna.compat.permissions;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/** Counters shown by {@code /iunacompat status}. */
public final class BridgeStats {
    private final AtomicLong allowed = new AtomicLong();
    private final AtomicLong denied = new AtomicLong();
    private final AtomicLong undecided = new AtomicLong();
    private final AtomicLong optionsAnswered = new AtomicLong();
    private final AtomicLong optionsMissed = new AtomicLong();
    private final AtomicLong errors = new AtomicLong();
    private final AtomicReference<String> lastError = new AtomicReference<>();

    void record(Verdict verdict) {
        switch (verdict) {
            case TRUE -> allowed.incrementAndGet();
            case FALSE -> denied.incrementAndGet();
            case DEFAULT -> undecided.incrementAndGet();
        }
    }

    void recordOption(boolean answered) {
        (answered ? optionsAnswered : optionsMissed).incrementAndGet();
    }

    void recordError(Throwable error) {
        errors.incrementAndGet();
        lastError.set(error.getClass().getSimpleName() + ": " + error.getMessage());
    }

    public long allowed() {
        return allowed.get();
    }

    public long denied() {
        return denied.get();
    }

    public long undecided() {
        return undecided.get();
    }

    public long optionsAnswered() {
        return optionsAnswered.get();
    }

    public long optionsMissed() {
        return optionsMissed.get();
    }

    public long errors() {
        return errors.get();
    }

    public String lastError() {
        return lastError.get();
    }
}
