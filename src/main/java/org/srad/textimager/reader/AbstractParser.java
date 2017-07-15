package main.java.org.srad.textimager.reader;

import main.java.org.srad.textimager.reader.type.ElementType;
import main.java.org.srad.textimager.reader.type.ElementTypes;

import java.util.stream.Stream;

abstract public class AbstractParser {
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
}
