package com.github.srad.textimager.reader.type;

import javax.xml.namespace.QName;

/** Attributes not final because we need an object reference until the internal text value can be set */
final public class Sofa {
    private String id;
    private String text;

    public Sofa() {}

    public Sofa(final String id, final String text) {
        this.id = id;
        this.text = text;
    }

    public void set(final String id, final String text) {
        this.id = id;
        this.text = text;
    }

    public String getText() { return text; }
    public String getText(int begin, int end) { return  text.substring(begin, end); }
    public String getCharAt(int i) { return String.valueOf(getText().charAt(i)); }

    public static QName getElementInfo() { return new QName("http:///uima/cas.ecore", "Sofa"); }

    public String getId() {
        if (id == null) {
            throw new RuntimeException("Sofa::getId is null");
        }
        return id;
    }
}
