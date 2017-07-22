package com.github.srad.textimager.reader;

import com.github.srad.textimager.reader.type.ElementType;
import com.github.srad.textimager.reader.type.ElementTypes;

import java.io.File;
import java.util.Map;
import java.util.stream.Stream;

abstract public class AbstractParser {

    final private File file;

    abstract public void parse() throws Exception;

    abstract public String getDocumentId();

    abstract public String getDocumentTitle();

    abstract public Map<String, String> getDocumentMeta();

    public AbstractParser(File file) { this.file = file; }

    private ElementTypes elements = new ElementTypes();

    protected void addElement(ElementType type) {
        elements.add(type);
    }

    public File getFile() { return file; }

    public boolean isPoisonPill() { return false; }

    public ElementTypes getElements() {
        return elements;
    }

    public Stream<ElementType> filterType(Class<? extends ElementType> t) {
        return getElements()
                .stream()
                .filter(e -> e.getName().equals(t.getSimpleName()));
    }
}
