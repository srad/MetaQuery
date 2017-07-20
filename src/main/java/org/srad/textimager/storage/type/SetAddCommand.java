package main.java.org.srad.textimager.storage.type;

public class SetAddCommand extends AbstractStorageCommand<String> {
    public SetAddCommand(String key, String data) {
        super(key, data);
    }
}
