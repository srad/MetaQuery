package com.github.srad.metaquery.dbms.storage.type;

public class IncrementCommand extends AbstractStorageCommand<Integer> {
    public IncrementCommand(String key, Integer data) {
        super(key, data);
    }
}
