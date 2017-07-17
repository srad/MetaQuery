package main.java.org.srad.textimager.storage;

/** Pseudoobject for queue end */
public class StoragePoisonPill extends AbstractElementStore<String> {
    public StoragePoisonPill() {
        super(null, null);
    }

    @Override
    public boolean isPoisonPill() {
        return true;
    }
}
