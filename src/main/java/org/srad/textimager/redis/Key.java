package main.java.org.srad.textimager.redis;

import main.java.org.srad.textimager.main;

import java.util.HashMap;

/**
 * Redis Key name mangement
 */
public class Key {
    final public static String DocSofaToDocId = create("doc", "id", "sofa");

    final public static String DocTitle = create("doc", "title");

    final public static String DocContent = create("doc", "content");

    final public static String TF_GLOBAL = create("count", "total", "tf");

    final public static String TotalLemmaCount = create("count", "total", "lemma");

    final public static String TotalChars = create("count", "total", "chars");

    public static String create(String... args) {
        return String.join(":", args).toLowerCase();
    }

    public static <U, V> HashMap<U, V>createMap(U key, V value) {
        HashMap<U, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
}
