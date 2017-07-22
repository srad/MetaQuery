package com.github.srad.textimager.storage.type;

import java.util.HashMap;
import java.util.Map;

public class MapCommand<V> extends AbstractStorageCommand<Map<String, String>> {

    public MapCommand(String key, Map<String, V> data) {
        super(key, new HashMap<String, String>() {{
            data.forEach((String k, V v) -> {
                this.put(k, String.valueOf(v));
            });
        }});
    }
}
