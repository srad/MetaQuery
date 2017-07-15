package main.java.org.srad.textimager.reader.type;

import javax.xml.namespace.QName;

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

    public static QName getElementInfo() { return new QName("http:///uima/cas.ecore", "Sofa"); }
}
