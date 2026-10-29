package club.iuna.compat.permissions.fabric.fakeapi;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Test double for {@code net.fabricmc.fabric.api.event.Event}: same public shape, array-backed. */
public final class Event<T> {
    private final Class<T> type;
    private final Function<T[], T> combiner;
    private final List<T> listeners = new ArrayList<>();

    private Event(Class<T> type, Function<T[], T> combiner) {
        this.type = type;
        this.combiner = combiner;
    }

    public static <T> Event<T> create(Class<T> type, Function<T[], T> combiner) {
        return new Event<>(type, combiner);
    }

    @SuppressWarnings("unchecked")
    public T invoker() {
        return combiner.apply(listeners.toArray((T[]) Array.newInstance(type, 0)));
    }

    public void register(T listener) {
        listeners.add(listener);
    }
}
