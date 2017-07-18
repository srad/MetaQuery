package main.java.org.srad.textimager.storage.type;

import java.util.Map;

/** Just to recognize that it's a document to count the stored document within the database */
public class DocumentCommand extends MapCommand {
    public DocumentCommand(String key, Map data) {
        super(key, data);
    }
}
