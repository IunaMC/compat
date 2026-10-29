package club.iuna.compat.permissions;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeCatalogTest {
    private static final Pattern VALID = Pattern.compile("[A-Za-z0-9_.-]+");

    @Test
    void parseSkipsCommentsAndBlankLines() {
        List<String> nodes = NodeCatalog.parse(new BufferedReader(new StringReader("# c\n\n  a.b  \n#d\nc.d\n")));

        assertEquals(List.of("a.b", "c.d"), nodes);
    }

    @Test
    void bundledCatalogIsWellFormed() {
        List<String> nodes = NodeCatalog.load();

        assertTrue(nodes.size() > 100, "the catalog should not be empty");
        assertEquals(nodes.size(), new HashSet<>(nodes).size(), "no duplicate nodes");
        for (String node : nodes) {
            assertTrue(VALID.matcher(node).matches(), "invalid node: " + node);
            assertTrue(node.contains("."), "node without a namespace: " + node);
        }
    }

    @Test
    void bundledCatalogCoversTheModsOfThePack() {
        List<String> nodes = NodeCatalog.load();

        for (String prefix : List.of("banhammer.", "styledchat.", "styledplayerlist.", "essentialcommands.", "easyauth.")) {
            assertTrue(nodes.stream().anyMatch(node -> node.startsWith(prefix)), "no node for " + prefix);
        }
    }
}
