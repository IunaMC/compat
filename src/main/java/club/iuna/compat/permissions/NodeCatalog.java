package club.iuna.compat.permissions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** The nodes in {@code known-nodes.txt}. */
public final class NodeCatalog {
    static final String RESOURCE = "known-nodes.txt";

    private NodeCatalog() {
    }

    public static List<String> load() {
        try (InputStream in = NodeCatalog.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing resource " + RESOURCE);
            }
            return parse(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static List<String> parse(BufferedReader reader) {
        return reader.lines()
                .map(String::strip)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .toList();
    }
}
