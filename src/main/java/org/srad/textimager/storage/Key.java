package main.java.org.srad.textimager.storage;

/**
 * KV-Storage keys
 */
public class Key {
    final public static String DocCount = create("doc", "count", "total");

    final public static String DocTitle = create("doc", "title");

    final public static String TotalTf = create("doc", "count", "tf", "total");

    final public static String TotalLemmaCount = create("count", "total", "lemma");

    final public static String TotalChars = create("count", "total", "chars");

    public static String create(String... args) {
        return String.join(":", args).toLowerCase();
    }
}
