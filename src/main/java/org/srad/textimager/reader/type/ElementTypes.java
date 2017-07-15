package main.java.org.srad.textimager.reader.type;

import java.util.ArrayList;
import java.util.stream.Collectors;

final public class ElementTypes extends ArrayList<ElementType> {
    @Override
    public String toString() {
        return this.stream().map(Object::toString)
                .collect(Collectors.joining(", "));
    }
}
