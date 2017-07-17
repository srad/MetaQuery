package main.java.org.srad.textimager.reader;

import main.java.org.srad.textimager.reader.type.ElementType;
import main.java.org.srad.textimager.reader.type.ElementTypes;

import java.io.File;
import java.util.Map;
import java.util.stream.Stream;

abstract public class AbstractParser {

    final private File file;

    public AbstractParser(File file) { this.file = file; }

    public boolean isPoisonPill() { return false; }

    private ElementTypes elements = new ElementTypes();

    protected void addElement(ElementType type) {
        elements.add(type);
    }

    public ElementTypes getElements() {
       return elements;
    }

    public Stream<ElementType> filterType(Class<? extends ElementType> t) {
        return getElements()
                .parallelStream()
                .filter(e -> e.getName().equals(t.getSimpleName()));
    }

    abstract public void parse() throws Exception;

    abstract public String getDocumentId();

    abstract public Map<String, String> getDocumentMeta();

    public File getFile() { return file; }
}
