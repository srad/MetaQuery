package com.github.srad.textimager.storage.type;

public class IncrementCommand extends AbstractStorageCommand<Integer> {
    public IncrementCommand(String key, Integer data) {
        super(key, data);
    }
}
