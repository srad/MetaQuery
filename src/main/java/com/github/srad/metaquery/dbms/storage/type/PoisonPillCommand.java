package com.github.srad.metaquery.dbms.storage.type;

/** Pseudoobject for queue end */
public class PoisonPillCommand extends AbstractStorageCommand<String> {
    public PoisonPillCommand() {
        super(null, null);
    }

    @Override
    public boolean isPoisonPill() {
        return true;
    }
}
