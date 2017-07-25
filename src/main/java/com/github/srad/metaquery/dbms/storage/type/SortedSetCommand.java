package com.github.srad.metaquery.dbms.storage.type;

import java.util.Map;

public class SortedSetCommand extends AbstractStorageCommand<Map<String, Long>> {
    public SortedSetCommand(String key, Map<String, Long> data) {
        super(key, data);
    }
}
