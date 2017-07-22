package com.github.srad.textimager.reader.type;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

abstract public class ElementType {
    final public Sofa sofa;
    final public String id;
    final public int begin;
    final public int end;

    final public static String TypeSeparator = ">>>>";

    /** Lazy */
    private String extractedText = null;

    public ElementType(final Sofa sofa, final String id, final String begin, final String end) {
        this(sofa, id, Integer.valueOf(begin), Integer.valueOf(end));
    }

    public ElementType(final Sofa sofa, final String id, final int begin, final int end) {
        this.sofa = sofa;
        this.id = id;
        this.begin = begin;
        this.end = end;
    }

    public ElementType(final Sofa sofa, final String id, final int begin, final int end, String text) {
        this.sofa = sofa;
        this.id = id;
        this.begin = begin;
        this.end = end;
        this.extractedText = text;
    }

    public String getText() {
        if (extractedText == null) {
            extractedText = sofa.getText(this.begin, this.end);
        }
        return extractedText;
    }

    public String getName() { return this.getClass().getSimpleName(); }

    public String getNormalizedText() { return getText().toLowerCase(); }

    public String getTextWithType() {
        return String.format("%s%s%s", getClass().getSimpleName(), TypeSeparator, getNormalizedText());
    }

    public String getTypeName() { return getClass().getSimpleName(); }

    @Override
    public String toString() { return String.format("%s(id: %s, text: %s, begin: %s, end: %s)", getName(), id, getText(), begin, end); }

    public static QName getElementInfo() { throw new NotImplementedException(); }

    public Map<String, String> toMap() {
        return new HashMap<String, String>() {{
            put("sofa", sofa.getId());
            put("id", id);
            put("begin", String.valueOf(begin));
            put("end", String.valueOf(end));
            put("text", getText());
        }};
    }
}
