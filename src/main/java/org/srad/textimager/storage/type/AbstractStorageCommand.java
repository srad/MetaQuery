package main.java.org.srad.textimager.storage.type;

abstract public class AbstractStorageCommand<T> {
    final public String key;
    final public T data;

    public AbstractStorageCommand(final String key, final T data) {
        this.key = key;
        this.data = data;
    }

    public boolean isPoisonPill() { return  false; }
}
