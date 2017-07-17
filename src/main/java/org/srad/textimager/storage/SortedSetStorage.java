package main.java.org.srad.textimager.storage;

import java.util.Map;

public class SortedSetStorage extends AbstractElementStore<Map<String, Long>> {
    public SortedSetStorage(String key, Map<String, Long> data) {
        super(key, data);
    }
}
