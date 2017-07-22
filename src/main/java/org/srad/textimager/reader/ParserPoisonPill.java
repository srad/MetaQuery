package org.srad.textimager.reader;

import java.util.Map;

/** Dummy object for queue end. */
public class ParserPoisonPill extends AbstractParser {
    public ParserPoisonPill() { super(null);}

    @Override
    public boolean isPoisonPill() {
        return true;
    }

    @Override
    public void parse() {

    }

    @Override
    public String getDocumentId() {
        return null;
    }

    @Override
    public String getDocumentTitle() { return null; }

    @Override
    public Map<String, String> getDocumentMeta() {
        return null;
    }
}
