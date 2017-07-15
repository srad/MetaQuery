package main.java.org.srad.textimager.reader.type;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.namespace.QName;
import java.util.HashMap;

abstract public class ElementType {
    final public Sofa sofa;
    final public String id;
    final public int begin;
    final public int end;
    private String extractedText = null;

    protected HashMap<String, Integer> counts;

    public ElementType(final Sofa sofa, final String id, final int begin, final int end) {
        this.sofa = sofa;
        this.id = id;
        this.begin = begin;
        this.end = end;
    }

    public ElementType(final Sofa sofa, final String id, final String begin, final String end) {
        this(sofa, id, Integer.valueOf(begin), Integer.valueOf(end));
    }

    public static QName getElementInfo() { throw new NotImplementedException(); }

    public String getName() { return this.getClass().getSimpleName(); }

    public String getText() {
        if (extractedText == null) {
            extractedText = sofa.getText(this.begin, this.end);
        }
        return extractedText;
    }

    public String getNormalizedText() {
        return getText().toLowerCase();
    }

    @Override
    public String toString() {
        return String.format("%s(id: %s, text: %s, begin: %s, end: %s)", getName(), id, getText(), begin, end);
    }
}
