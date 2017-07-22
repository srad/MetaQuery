package org.srad.textimager.storage.type;

import java.util.Map;

public class SortedSetCommand extends AbstractStorageCommand<Map<String, Long>> {
    public SortedSetCommand(String key, Map<String, Long> data) {
        super(key, data);
    }
}
