package main.java.org.srad.textimager.storage;

abstract public class AbstractElementStore<T> {
    final public String key;
    final public T data;

    public AbstractElementStore(final String key, final T data) {
        this.key = key;
        this.data = data;
    }

    public boolean isPoisonPill() { return  false; }
}
