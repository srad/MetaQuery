package main.java.org.srad.textimager.storage;

import java.util.HashMap;
import java.util.Map;

public class MapStorage<V> extends AbstractElementStore<Map<String, String>> {

    public MapStorage(String key, Map<String, V> data) {
        super(key, new HashMap<String, String>() {{
            data.forEach((String k, V v) -> {
                this.put(k, String.valueOf(v));
            });
        }});
    }
}
